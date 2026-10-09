package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.model.Achievement
import pe.greenminds.ecomind.gamification.domain.model.AchievementAward

// Backend develop: AchievementResource and AchievementAwardResource.
// Both routes return arrays (not RankingPageDto). A remote adapter must collect every page.
// GET /api/v1/gamification/achievements?scope=INDIVIDUAL&page=&size=
data class AchievementDto(
    val id: String, val code: String, val name: String, val description: String,
    val scope: String, val metric: String, val target: Long, val active: Boolean
) {
    fun toDomain() = Achievement(id, code, name, description, scope, metric, target, active)
}

// GET /api/v1/gamification/me/achievements?page=&size=; identity comes from JWT.
data class AchievementAwardDto(
    val id: String, val achievementId: String, val scope: String, val beneficiaryId: Long?,
    val sourceEventId: String, val awardedAt: String, val communityId: Long?
) {
    fun toDomain() = AchievementAward(id, achievementId, scope, beneficiaryId, sourceEventId, awardedAt, communityId)
}
