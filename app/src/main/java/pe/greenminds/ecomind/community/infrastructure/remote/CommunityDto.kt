package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityDto(
    val id: Long,
    val name: String,
    val description: String?,
    val type: String,
    val topic: String?,
    val locality: String?,
    @SerializedName("member_limit")
    val memberLimit: Int?,
    @SerializedName("icon_url")
    val iconUrl: String?,
    @SerializedName("created_by")
    val createdBy: Long?
)