package pe.greenminds.ecomind.community.infrastructure.remote

import pe.greenminds.ecomind.community.domain.model.CommunityEvent

// PROVISIONAL: the web services have no Community context yet.
// The route comes from the technical story of community events and the fields are
// only the ones the screen needs. Both must be checked when the context exists.

// GET /api/v1/community/events
data class CommunityEventDto(
    val id: Long,
    val title: String,
    val description: String,
    // yyyy-MM-dd
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
