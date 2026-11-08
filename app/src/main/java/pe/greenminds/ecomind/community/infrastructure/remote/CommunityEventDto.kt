package pe.greenminds.ecomind.community.infrastructure.remote

data class CommunityEventDto(
    val id: Long,
    val title: String,
    val description: String,
    val date: String
)
