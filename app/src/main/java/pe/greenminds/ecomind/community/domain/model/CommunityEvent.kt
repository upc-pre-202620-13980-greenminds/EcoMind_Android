package pe.greenminds.ecomind.community.domain.model

data class CommunityEvent(
    val id: Long,
    val title: String,
    val description: String,
    val date: String
)
