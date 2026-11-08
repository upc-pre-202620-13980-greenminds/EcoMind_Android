package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityEventDto

fun CommunityEventDto.toDomain(): CommunityEvent {
    return CommunityEvent(
        id = id,
        title = title,
        description = description,
        date = date
    )
}
