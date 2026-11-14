package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CreateCommunityGoalRequest(
    @SerializedName("community_id") val communityId: Long,
    val topic: String
)