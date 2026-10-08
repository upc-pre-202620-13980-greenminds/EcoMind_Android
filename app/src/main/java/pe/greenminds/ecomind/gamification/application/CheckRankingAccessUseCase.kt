package pe.greenminds.ecomind.gamification.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

// Rule of the design: the ranking opens only for users who completed at least one activity
class CheckRankingAccessUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val rankingRepository: RankingRepository
) {

    suspend operator fun invoke(): Result<Boolean> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))

        // Completing an activity gives ecopoints, so having any means the user took part
        return rankingRepository.getParticipants(RankingType.GLOBAL).map { participants ->
            val total = participants.find { it.id == session.accountId }?.totalEcopoints ?: 0
            total > 0
        }
    }
}
