package pe.greenminds.ecomind.gamification.domain.model

data class Achievement(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val scope: String,
    val metric: String,
    val target: Long,
    val active: Boolean
)

data class AchievementAward(
    val id: String,
    val achievementId: String,
    val scope: String,
    val beneficiaryId: Long?,
    val sourceEventId: String,
    val awardedAt: String,
    val communityId: Long?
)

data class AchievementEntry(val achievement: Achievement, val award: AchievementAward?)

data class AchievementCollection(
    val entries: List<AchievementEntry>,
    val isSimulated: Boolean
) {
    val earned: List<AchievementEntry> get() = entries.filter { it.award != null }
    val available: List<AchievementEntry> get() = entries.filter { it.award == null && it.achievement.active }
}

class AchievementSessionRequiredException : IllegalStateException("A current session is required")
