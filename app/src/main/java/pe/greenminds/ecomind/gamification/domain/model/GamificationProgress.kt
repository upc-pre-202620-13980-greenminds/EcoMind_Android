package pe.greenminds.ecomind.gamification.domain.model

data class UserProgress(val userId: Long, val ecopoints: Long, val currentStreak: Int, val longestStreak: Int,
    val lastActivityDate: String?, val lastProtectedDate: String?)
data class RewardHistory(val id: String, val source: String, val ecopoints: Long, val gems: Int, val occurredAt: String)
data class AchievementGroup(val id: Long, val name: String, val scope: String)
data class GamificationOverview(val progress: UserProgress, val groups: List<AchievementGroup>, val familyEcopoints: Long?, val simulated: Boolean)
data class AchievementShare(val requestId: String, val awardId: String, val communityId: Long, val status: String, val publicationId: Long?)
