package pe.greenminds.ecomind.community.domain.model

// Environmental activity organized by a community
data class CommunityEvent(
    val id: Long,
    val title: String,
    val description: String,
    // Day of the event as yyyy-MM-dd
    val date: String
)
