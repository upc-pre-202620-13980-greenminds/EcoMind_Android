package pe.greenminds.ecomind.quests.infrastructure.implementation

import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardHistoryApi
import pe.greenminds.ecomind.quests.infrastructure.remote.FinishMinigameAttemptRequest
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptApi
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptDto
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptStatus
import pe.greenminds.ecomind.quests.infrastructure.remote.StartMinigameAttemptRequest
import retrofit2.HttpException

data class MinigameReward(val ecopoints: Long, val gems: Int)

class RemoteMinigameAttempts @Inject constructor(
    private val attempts: MinigameAttemptApi,
    private val rewards: RewardHistoryApi
) {
    suspend fun getMinigame(minigameId: Long) = attempts.getMinigame(minigameId)

    suspend fun start(questId: Long, minigameId: Long): Long {
        return try {
            attempts.start(StartMinigameAttemptRequest(questId)).id
        } catch (error: HttpException) {
            if (error.code() != 409) throw error
            attempts.getAttempts(minigameId).firstOrNull {
                it.questId == questId && it.status == MinigameAttemptStatus.STARTED
            }?.id ?: throw error
        }
    }

    suspend fun finish(
        attemptId: Long,
        minigameId: Long,
        score: Int
    ): MinigameAttemptDto {
        return try {
            attempts.finish(attemptId, FinishMinigameAttemptRequest(score))
        } catch (error: HttpException) {
            // The finish may have committed before the response was lost.
            if (error.code() != 422) throw error
            attempts.getAttempts(minigameId).firstOrNull {
                it.id == attemptId && it.status == MinigameAttemptStatus.COMPLETED
            } ?: throw error
        }
    }

    suspend fun cancel(attemptId: Long, minigameId: Long) {
        try {
            attempts.cancel(attemptId)
        } catch (error: HttpException) {
            if (error.code() != 422) throw error
            if (attempts.getAttempts(minigameId).none {
                    it.id == attemptId && it.status == MinigameAttemptStatus.CANCELLED
                }) throw error
        }
    }

    suspend fun rewardFor(attempt: MinigameAttemptDto): MinigameReward? {
        // Gamification derives source.executionId from this Quests completion key.
        val executionId = UUID.nameUUIDFromBytes(
            "minigame-attempt:${attempt.id}".toByteArray(Charsets.UTF_8)
        ).toString()
        val completedAt = OffsetDateTime.parse(requireNotNull(attempt.endDate)).toInstant()
        // Allow for persistence rounding of the event timestamp.
        val from = completedAt.minusSeconds(1).toString()
        val to = completedAt.plusSeconds(1).toString()
        var page = 0
        do {
            val result = rewards.history(from, to, page++)
            val entry = result.items.firstOrNull {
                it.source.type == "MINIGAME" && it.source.executionId == executionId
            }
            if (entry != null) {
                return MinigameReward(
                    entry.grantedReward.ecopoints,
                    entry.grantedReward.gems
                )
            }
        } while (result.hasNext)
        return null
    }
}
