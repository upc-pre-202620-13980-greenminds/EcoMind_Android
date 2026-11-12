package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityEventDto

fun CommunityEventDto.toDomain() = CommunityEvent(
    id = id,
    communityId = communityId,
    title = name,
    description = description,
    date = date,
    startTime = startTime,
    location = location,
    capacity = capacity,
    imageUrl = imageUrl
)