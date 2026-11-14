package pe.greenminds.ecomind.gamification.domain.repositories

import pe.greenminds.ecomind.gamification.domain.model.*

interface ProgressRepository {
    val isSimulated: Boolean
    suspend fun getOverview(): Result<GamificationOverview>
    suspend fun getHistory(fromMillis: Long, toMillis: Long): Result<List<RewardHistory>>
    suspend fun getGroupAchievements(group: AchievementGroup): Result<AchievementCollection>
    suspend fun getShare(awardId: String): Result<AchievementShare?>
    suspend fun share(awardId: String, communityId: Long): Result<AchievementShare>
}
