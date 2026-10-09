package pe.greenminds.ecomind.community.infrastructure.remote

import pe.greenminds.ecomind.community.domain.model.CommunityEvent


data class CommunityEventDto(
    val id: Long,
    val title: String,
    val description: String,
    val date: String
)

fun CommunityEventDto.toDomain(): CommunityEvent {
    return CommunityEvent(
        id = id,
        title = title,
        description = description,
        date = date
    )
}
