package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityMembershipDto(
    val id: Long,
    @SerializedName("community_id")
    val communityId: Long,
    @SerializedName("user_id")
    val userId: Long,
    val role: String
)