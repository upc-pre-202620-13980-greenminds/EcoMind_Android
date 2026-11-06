package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation

data class InvitationsUiState(
    val invitations: List<CollabQuestInvitation> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false
)
