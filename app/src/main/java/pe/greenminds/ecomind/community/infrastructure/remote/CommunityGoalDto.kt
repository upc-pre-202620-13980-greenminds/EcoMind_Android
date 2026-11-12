package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityGoalDto(
    val id: Long,
    @SerializedName("community_id") val communityId: Long,
    val topic: String,
    val title: String,
    val target: Int,
    val progress: Int,
    val participants: Int,
    val status: String
)