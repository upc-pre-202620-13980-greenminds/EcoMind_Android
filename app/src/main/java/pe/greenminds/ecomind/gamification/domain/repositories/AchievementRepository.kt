package pe.greenminds.ecomind.gamification.domain.repositories

import pe.greenminds.ecomind.gamification.domain.model.Achievement
import pe.greenminds.ecomind.gamification.domain.model.AchievementAward

interface AchievementRepository {
    val isSimulated: Boolean
    suspend fun getCatalog(): Result<List<Achievement>>
    suspend fun getMyAwards(): Result<List<AchievementAward>>
}
