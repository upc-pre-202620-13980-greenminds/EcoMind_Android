package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityDto

fun CommunityDto.toDomain(): Community = Community(
    id = id,
    name = name,
    description = description,
    type = CommunityType.valueOf(type.uppercase()),
    topic = topic,
    locality = locality,
    memberLimit = memberLimit,
    iconUrl = iconUrl,
    createdBy = createdBy
)

