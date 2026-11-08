package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityMembership
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityRole
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityMembershipDto

fun CommunityMembershipDto.toDomain(): CommunityMembership = CommunityMembership(
    id = id,
    communityId = communityId,
    userId = userId,
    role = CommunityRole.valueOf(role.uppercase())
)