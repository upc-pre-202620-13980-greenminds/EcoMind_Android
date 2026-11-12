package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityAchievement
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityAchievementDto

fun CommunityAchievementDto.toDomain() = CommunityAchievement(
    id = id,
    communityId = communityId,
    title = title,
    description = description,
    communityGoalId = communityGoalId
)