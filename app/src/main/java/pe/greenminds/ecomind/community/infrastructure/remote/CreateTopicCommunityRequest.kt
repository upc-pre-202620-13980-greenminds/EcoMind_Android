package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CreateTopicCommunityRequest(
    val name: String,
    val description: String?,
    val topic: String,
    @SerializedName("member_limit")
    val memberLimit: Int,
    @SerializedName("icon_url")
    val iconUrl: String?
)