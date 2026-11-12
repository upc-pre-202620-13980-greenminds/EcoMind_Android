package pe.greenminds.ecomind.community.domain.entity

import pe.greenminds.ecomind.community.domain.valueobjects.CommunityRole

data class CommunityMembership(
    val id: Long,
    val communityId: Long,
    val userId: Long,
    val role: CommunityRole
)