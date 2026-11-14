package pe.greenminds.ecomind.community.domain.entity

data class CommunityGoalPanel(
    val goal: CommunityGoal?,
    val canCreate: Boolean,
    val joined: Boolean,
    val otherGoalActive: Boolean,
    val memberCount: Int,
    val targetPreview: Int
)