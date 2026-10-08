package pe.greenminds.ecomind.gamification.infrastructure.local

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.gamification.infrastructure.remote.millisToInstant
import pe.greenminds.ecomind.gamification.infrastructure.remote.toDomain
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalRankingRepository @Inject constructor(
    private val dataSource: LocalRankingDataSource,
    // Plays the role of the access token: the web services read the user from it
    private val sessionRepository: SessionRepository
) : RankingRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L

        // Largest page the web services accept
        private const val PAGE_SIZE = 100
    }

    override suspend fun getParticipants(type: RankingType): Result<List<RankingParticipant>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)
        val userId = currentUserId() ?: return Result.failure(IllegalStateException("No session"))

        // The pages are read one after another until the last one
        val participants = mutableListOf<RankingParticipant>()
        var page = 0
        do {
            val dto = dataSource.participantsPage(type, userId, page, PAGE_SIZE)
            participants.addAll(dto.items.map { it.toDomain() })
            page++
        } while (dto.hasNext)

        return Result.success(participants)
    }

    override suspend fun getTransactions(
        type: RankingType,
        fromMillis: Long,
        toMillis: Long
    ): Result<List<RankingTransaction>> {
        delay(SIMULATED_DELAY_MILLIS)
        val userId = currentUserId() ?: return Result.failure(IllegalStateException("No session"))

        // The web services receive the limits as instants in UTC
        val from = millisToInstant(fromMillis)
        val to = millisToInstant(toMillis)

        val transactions = mutableListOf<RankingTransaction>()
        var page = 0
        do {
            val dto = dataSource.transactionsPage(type, userId, from, to, page, PAGE_SIZE)
            transactions.addAll(dto.items.map { it.toDomain() })
            page++
        } while (dto.hasNext)

        return Result.success(transactions)
    }

    private suspend fun currentUserId(): Long? {
        return sessionRepository.getSession().first()?.accountId
    }
}
