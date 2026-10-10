package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import javax.inject.Inject

class RemoteRankingRepository @Inject constructor(private val api: GamificationApi, private val access: RemoteAccess) : RankingRepository {
    override suspend fun getParticipants(type: RankingType) = access.authenticated { session ->
        collectPages { api.participants(session, type.name, it) }.map { it.toDomain() }
    }
    override suspend fun getTransactions(type: RankingType, fromMillis: Long, toMillis: Long) = access.authenticated { session ->
        collectPages { api.transactions(session, type.name, millisToInstant(fromMillis), millisToInstant(toMillis), it) }.map { it.toDomain() }
    }
}
