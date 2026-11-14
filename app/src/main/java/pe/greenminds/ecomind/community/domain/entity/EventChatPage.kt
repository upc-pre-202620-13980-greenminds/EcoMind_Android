package pe.greenminds.ecomind.community.domain.entity

data class EventChatPage(
    val eventId: Long,
    val authorId: Long,
    val expiresAt: String,
    val messages: List<EventChatMessage>
)