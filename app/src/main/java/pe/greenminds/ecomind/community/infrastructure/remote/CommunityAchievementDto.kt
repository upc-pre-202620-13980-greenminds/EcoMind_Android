package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityAchievementDto(
    val id: Long,
    @SerializedName("community_id") val communityId: Long,
    val title: String,
    val description: String?,
    @SerializedName("community_goal_id") val communityGoalId: Long?
)