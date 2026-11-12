package pe.greenminds.ecomind.community.domain.entity

data class CommunityGoal(
    val id: Long,
    val communityId: Long,
    val topic: String,
    val title: String,
    val target: Int,
    val progress: Int,
    val participants: Int,
    val status: String
)