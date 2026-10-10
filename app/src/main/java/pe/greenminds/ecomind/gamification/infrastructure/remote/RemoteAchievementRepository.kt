package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import javax.inject.Inject

class RemoteAchievementRepository @Inject constructor(private val api: GamificationApi, private val access: RemoteAccess) : AchievementRepository {
    override val isSimulated = false
    override suspend fun getCatalog() = access.authenticated { session -> collectArrayPages { api.catalog(session, it) }.map { it.toDomain() } }
    override suspend fun getMyAwards() = access.authenticated { session -> collectArrayPages { api.awards(session, it) }.map { it.toDomain() } }
}
