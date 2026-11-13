package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityGoalPanel
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityGoalPanelDto

fun CommunityGoalPanelDto.toDomain() = CommunityGoalPanel(
    goal = goal?.toDomain(),
    canCreate = canCreate,
    joined = joined,
    otherGoalActive = otherGoalActive,
    memberCount = memberCount,
    targetPreview = targetPreview
)