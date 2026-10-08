package pe.greenminds.ecomind.quests.interfaces.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.SearchQuestsUseCase
import pe.greenminds.ecomind.quests.domain.model.QuestCategory
import pe.greenminds.ecomind.quests.domain.model.QuestFilter
import pe.greenminds.ecomind.quests.domain.model.QuestType
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestListRoute
import javax.inject.Inject

@HiltViewModel
class QuestListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchQuests: SearchQuestsUseCase
) : ViewModel() {

    companion object {
        // Pause after the last key before searching, so not every letter triggers a request
        private const val TYPING_PAUSE_MILLIS = 300L
    }

    // The filter chosen in the category menu arrives as arguments of the route
    private val route = savedStateHandle.toRoute<QuestListRoute>()
    private val category = route.category?.let { QuestCategory.valueOf(it) }
    private val questType = route.questType?.let { QuestType.valueOf(it) }

    private val _state = MutableStateFlow(QuestListUiState(focusSearch = route.focusSearch))
    val state: StateFlow<QuestListUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(waitForTyping = false)
    }

    fun onQueryChange(query: String) {
        _state.update { currentState ->
            currentState.copy(query = query)
        }
        search(waitForTyping = true)
    }

    // Used by the search icon, the keyboard action and "Try again"
    fun searchNow() {
        search(waitForTyping = false)
    }

    private fun search(waitForTyping: Boolean) {
        // A new search replaces the one that was still running
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (waitForTyping) {
                delay(TYPING_PAUSE_MILLIS)
            }

            _state.update { currentState ->
                currentState.copy(isLoading = true, hasError = false)
            }

            val filter = QuestFilter(
                title = _state.value.query,
                category = category,
                questType = questType
            )
            searchQuests(filter)
                .onSuccess { quests ->
                    _state.update { currentState ->
                        currentState.copy(isLoading = false, quests = quests)
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoading = false, hasError = true)
                    }
                }
        }
    }
}
