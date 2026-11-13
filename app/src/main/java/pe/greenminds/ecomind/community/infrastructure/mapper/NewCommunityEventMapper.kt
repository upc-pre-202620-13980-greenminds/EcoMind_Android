package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent
import pe.greenminds.ecomind.community.infrastructure.remote.CreateCommunityEventRequest

fun NewCommunityEvent.toRequest() = CreateCommunityEventRequest(
    communityId = communityId,
    name = name,
    description = description,
    date = date,
    startTime = startTime,
    location = location,
    latitude = latitude,
    longitude = longitude,
    capacity = capacity,
    imageUrl = imageUrl
)