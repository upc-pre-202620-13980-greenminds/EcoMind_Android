package pe.greenminds.ecomind.community.domain.entity

data class CommunityAchievement(
    val id: Long,
    val communityId: Long,
    val title: String,
    val description: String?,
    val communityGoalId: Long?
)