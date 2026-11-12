package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CreateCommunityEventRequest(
    @SerializedName("community_id") val communityId: Long,
    val name: String,
    val description: String?,
    val date: String,
    @SerializedName("start_time") val startTime: String,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    @SerializedName("image_url") val imageUrl: String?
)