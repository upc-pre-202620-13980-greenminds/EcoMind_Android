package pe.greenminds.ecomind.community.domain.entity

data class NewCommunityEvent(
    val communityId: Long,
    val name: String,
    val description: String?,
    val date: String,
    val startTime: String,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val imageUrl: String? = null
)