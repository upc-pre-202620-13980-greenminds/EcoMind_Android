package pe.greenminds.ecomind.quests.interfaces.list

import pe.greenminds.ecomind.quests.domain.model.Quest

data class QuestListUiState(
    val query: String = "",
    val quests: List<Quest> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    // True when the list was opened from "Search": the keyboard opens on the search field
    val focusSearch: Boolean = false
)
