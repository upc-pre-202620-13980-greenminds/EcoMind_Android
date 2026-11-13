package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class EventChatMessageDto(
    val id: Long,
    @SerializedName("event_id") val eventId: Long,
    @SerializedName("sender_id") val senderId: Long,
    @SerializedName("sender_name") val senderName: String,
    val text: String,
    @SerializedName("sent_at") val sentAt: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("is_mine") val isMine: Boolean
)