package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.domain.entity.Quest

data class QuestsUiState(
    val quests: List<Quest> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)
