package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class EventChatPageDto(
    @SerializedName("event_id") val eventId: Long,
    @SerializedName("author_id") val authorId: Long,
    @SerializedName("expires_at") val expiresAt: String,
    val messages: List<EventChatMessageDto>
)