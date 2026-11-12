package pe.greenminds.ecomind.community.domain.entity

data class EventChatMessage(
    val id: Long,
    val eventId: Long,
    val senderId: Long,
    val senderName: String,
    val text: String,
    val sentAt: String,
    val clientId: String,
    val isMine: Boolean
)