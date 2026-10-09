package pe.greenminds.ecomind.quests.interfaces.search

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
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestSearchRoute
import javax.inject.Inject

@HiltViewModel
class QuestSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchQuests: SearchQuestsUseCase
) : ViewModel() {

    companion object {
        private const val TYPING_PAUSE_MILLIS = 300L
    }

    private val route = savedStateHandle.toRoute<QuestSearchRoute>()
    private val category = route.category?.let(QuestCategory::valueOf)
    private val questType = route.questType?.let(QuestType::valueOf)

    private val _state = MutableStateFlow(QuestSearchUiState(focusSearch = route.focusSearch))
    val state: StateFlow<QuestSearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search(waitForTyping = false)
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        search(waitForTyping = true)
    }

    fun searchNow() {
        search(waitForTyping = false)
    }

    private fun search(waitForTyping: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (waitForTyping) delay(TYPING_PAUSE_MILLIS)
            _state.update { it.copy(isLoading = true, hasError = false) }

            searchQuests(
                query = _state.value.query,
                category = category,
                questType = questType
            )
                .onSuccess { quests ->
                    _state.update { it.copy(quests = quests, isLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, hasError = true) }
                }
        }
    }
}
