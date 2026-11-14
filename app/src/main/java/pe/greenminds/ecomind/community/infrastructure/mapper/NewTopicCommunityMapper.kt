package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.NewTopicCommunity
import pe.greenminds.ecomind.community.infrastructure.remote.CreateTopicCommunityRequest

fun NewTopicCommunity.toRequest() = CreateTopicCommunityRequest(
    name = name,
    description = description,
    topic = topic,
    memberLimit = memberLimit,
    iconUrl = iconUrl
)