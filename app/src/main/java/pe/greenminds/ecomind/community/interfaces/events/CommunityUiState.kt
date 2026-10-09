package pe.greenminds.ecomind.community.interfaces.events

import pe.greenminds.ecomind.community.domain.model.CommunityEvent
import pe.greenminds.ecomind.community.interfaces.sections.CommunitySection

data class CommunityUiState(
    val isLoading: Boolean = true,
    val events: List<CommunityEvent> = emptyList(),
    val selectedSection: CommunitySection = CommunitySection.EVENTS
)
