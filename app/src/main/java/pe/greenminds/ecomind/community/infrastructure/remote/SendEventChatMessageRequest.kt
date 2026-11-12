package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class SendEventChatMessageRequest(
    val text: String,
    @SerializedName("client_id") val clientId: String
)