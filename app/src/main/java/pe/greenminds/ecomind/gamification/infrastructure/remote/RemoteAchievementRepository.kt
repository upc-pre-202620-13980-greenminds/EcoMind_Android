package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import javax.inject.Inject

class RemoteAchievementRepository @Inject constructor(private val api: GamificationApi, private val access: RemoteAccess) : AchievementRepository {
    override val isSimulated = false
    override suspend fun getCatalog() = access.authenticated { _, token -> collectArrayPages { api.catalog(token, it) }.map { it.toDomain() } }
    override suspend fun getMyAwards() = access.authenticated { _, token -> collectArrayPages { api.awards(token, it) }.map { it.toDomain() } }
}
