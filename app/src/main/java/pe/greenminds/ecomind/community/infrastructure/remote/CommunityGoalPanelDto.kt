package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityGoalPanelDto(
    val goal: CommunityGoalDto?,
    @SerializedName("can_create") val canCreate: Boolean,
    val joined: Boolean,
    @SerializedName("other_goal_active") val otherGoalActive: Boolean,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("target_preview") val targetPreview: Int
)