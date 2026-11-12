package pe.greenminds.ecomind.community.domain.entity

data class CommunityEvent(
    val id: Long,
    val communityId: Long,
    val title: String,
    val description: String?,
    val date: String,
    val startTime: String?,
    val location: String?,
    val capacity: Int?,
    val imageUrl: String?
)