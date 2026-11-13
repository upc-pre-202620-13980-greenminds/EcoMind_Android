package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.EventChatMessage
import pe.greenminds.ecomind.community.domain.entity.EventChatPage
import pe.greenminds.ecomind.community.infrastructure.remote.EventChatMessageDto
import pe.greenminds.ecomind.community.infrastructure.remote.EventChatPageDto

fun EventChatMessageDto.toDomain() = EventChatMessage(
    id = id,
    eventId = eventId,
    senderId = senderId,
    senderName = senderName,
    text = text,
    sentAt = sentAt,
    clientId = clientId,
    isMine = isMine
)

fun EventChatPageDto.toDomain() = EventChatPage(
    eventId = eventId,
    authorId = authorId,
    expiresAt = expiresAt,
    messages = messages.map { it.toDomain() }
)