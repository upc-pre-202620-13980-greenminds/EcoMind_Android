package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.presentation.states.CommunitySection

data class CommunityUiState(
    val isLoading: Boolean = true,
    val events: List<CommunityEvent> = emptyList(),
    val selectedSection: CommunitySection = CommunitySection.EVENTS
)
