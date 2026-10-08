package pe.greenminds.ecomind.gamification.domain.repositories

import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction
import pe.greenminds.ecomind.gamification.domain.model.RankingType

interface RankingRepository {

    // Every participant of the ranking; the implementation reads all the pages
    suspend fun getParticipants(type: RankingType): Result<List<RankingParticipant>>

    // Transactions between fromMillis (included) and toMillis (not included)
    suspend fun getTransactions(
        type: RankingType,
        fromMillis: Long,
        toMillis: Long
    ): Result<List<RankingTransaction>>
}
