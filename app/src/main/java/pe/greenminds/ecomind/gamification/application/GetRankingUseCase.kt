package pe.greenminds.ecomind.gamification.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.Ranking
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.gamification.domain.services.RankingCalculator
import pe.greenminds.ecomind.gamification.domain.services.RankingPeriodRange
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade
import javax.inject.Inject

class GetRankingUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val rankingRepository: RankingRepository,
    private val usersContextFacade: UsersContextFacade
) {

    suspend operator fun invoke(type: RankingType, period: RankingPeriod): Result<Ranking> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))

        // In the ranking of families the highlighted row is the family of the user
        val currentId = if (type == RankingType.FAMILIES) {
            usersContextFacade.getFamilyIdOf(session.accountId)
        } else {
            session.accountId
        }

        val participants = rankingRepository.getParticipants(type)
            .getOrElse { return Result.failure(it) }

        val now = System.currentTimeMillis()
        val periodStart = RankingPeriodRange.startOf(period, now)

        val ecopointsById = if (periodStart == null) {
            // All-time: the total of each participant already comes with the participants
            participants.associate { it.id to it.totalEcopoints }
        } else {
            // A period: the web services return transactions and the app adds them up
            val transactions = rankingRepository.getTransactions(type, periodStart, now)
                .getOrElse { return Result.failure(it) }
            RankingCalculator.sumByParticipant(transactions)
        }

        return Result.success(RankingCalculator.rank(participants, ecopointsById, currentId))
    }
}
