package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.EventChatMessage
import pe.greenminds.ecomind.community.domain.entity.EventParticipation

data class CommunityEventsUiState(
    val communityId: Long? = null,
    val mine: Boolean = false,
    val loading: Boolean = false,
    val items: List<EventParticipation> = emptyList(),
    val search: String = "",
    val error: String? = null,
    val errorRes: Int? = null,
    val busy: Boolean = false,
    val joinTarget: EventParticipation? = null,
    val leaveTarget: EventParticipation? = null,
    val joinedAsFamily: Boolean? = null,
    val chat: EventParticipation? = null,
    val messages: List<EventChatMessage> = emptyList(),
    val chatAuthorId: Long? = null,
    val chatLoading: Boolean = false,
    val chatClosed: Boolean = false,
    val chatError: String? = null,
    val chatErrorRes: Int? = null,
    val draft: String = "",
    val sending: Boolean = false
) {
    val visibleItems: List<EventParticipation>
        get() = items.filter {
            it.event.title.contains(search.trim(), ignoreCase = true)
        }
}