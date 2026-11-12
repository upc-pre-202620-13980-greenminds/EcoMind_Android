package pe.greenminds.ecomind.quests

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardAmountDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardHistoryApi
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardHistoryEntryDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardHistoryPageDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.RewardSourceDto
import pe.greenminds.ecomind.quests.infrastructure.implementation.MinigameReward
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteMinigameAttempts
import pe.greenminds.ecomind.quests.infrastructure.remote.FinishMinigameAttemptRequest
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptApi
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptDto
import pe.greenminds.ecomind.quests.infrastructure.remote.MinigameAttemptStatus
import pe.greenminds.ecomind.quests.infrastructure.remote.StartMinigameAttemptRequest
import pe.greenminds.ecomind.quests.presentation.minigames.NativeMinigame

class RemoteMinigameAttemptsTest {
    @Test
    fun `only registered routes open the native waste game`() {
        assertEquals(NativeMinigame.COLLECT_WASTE, NativeMinigame.fromUrl("/games/collect-waste"))
        assertEquals(NativeMinigame.COLLECT_WASTE, NativeMinigame.fromUrl("/games/recoge-residuos"))
        assertNull(NativeMinigame.fromUrl("/games/autoclicker"))
    }

    @Test
    fun `confirmed zero on another page is distinct from missing reward`() = runBlocking {
        val attempt = completed(3, "2026-11-11T12:01:00-05:00")
        val repository = repository { from, to, page ->
            assertEquals("2026-11-11T17:00:59Z", from)
            assertEquals("2026-11-11T17:01:01Z", to)
            when (page) {
                0 -> RewardHistoryPageDto(listOf(reward(42, 50)), hasNext = true)
                else -> RewardHistoryPageDto(listOf(reward(3, 0)), hasNext = false)
            }
        }
        assertEquals(MinigameReward(0, 0), repository.rewardFor(attempt))
        assertNull(repository.rewardFor(attempt.copy(id = 4)))
    }

    private fun repository(
        readRewards: (String, String, Int) -> RewardHistoryPageDto
    ): RemoteMinigameAttempts {
        val attemptApi = object : MinigameAttemptApi {
            override suspend fun getAttempts(minigameId: Long) = error("Unexpected request")
            override suspend fun getMinigame(minigameId: Long) = error("Unexpected request")
            override suspend fun start(request: StartMinigameAttemptRequest) = error("Unexpected request")
            override suspend fun finish(attemptId: Long, request: FinishMinigameAttemptRequest) = error("Unexpected request")
            override suspend fun cancel(attemptId: Long) = error("Unexpected request")
        }
        val rewardApi = object : RewardHistoryApi {
            override suspend fun history(from: String, to: String, page: Int) = readRewards(from, to, page)
        }
        return RemoteMinigameAttempts(attemptApi, rewardApi)
    }

    private fun completed(id: Long, end: String) =
        MinigameAttemptDto(id, 1, MinigameAttemptStatus.COMPLETED, 10, end, true)

    private fun reward(attemptId: Long, ecopoints: Long): RewardHistoryEntryDto {
        val executionId = UUID.nameUUIDFromBytes("minigame-attempt:$attemptId".toByteArray()).toString()
        return RewardHistoryEntryDto(
            RewardSourceDto("MINIGAME", executionId),
            RewardAmountDto(ecopoints, 0)
        )
    }
}
