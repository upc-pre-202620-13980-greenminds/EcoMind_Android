package pe.greenminds.ecomind.community.interfaces.events

import pe.greenminds.ecomind.community.domain.model.CommunityEvent

data class CommunityUiState(
    val isLoading: Boolean = true,
    val events: List<CommunityEvent> = emptyList()
)
