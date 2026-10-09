package pe.greenminds.ecomind.quests.interfaces.search

import pe.greenminds.ecomind.quests.domain.entity.Quest

data class QuestSearchUiState(
    val query: String = "",
    val quests: List<Quest> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val focusSearch: Boolean = false
)
