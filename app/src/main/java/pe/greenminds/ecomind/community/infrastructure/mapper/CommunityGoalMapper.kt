package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityGoal
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityGoalDto

fun CommunityGoalDto.toDomain() = CommunityGoal(
    id = id,
    communityId = communityId,
    topic = topic,
    title = title,
    target = target,
    progress = progress,
    participants = participants,
    status = status
)