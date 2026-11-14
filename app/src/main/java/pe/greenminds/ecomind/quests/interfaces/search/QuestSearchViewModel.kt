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
import pe.greenminds.ecomind.quests.application.usecase.SearchQuestsUseCase
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestSearchRoute
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStore
import javax.inject.Inject

@HiltViewModel
class QuestSearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchQuests: SearchQuestsUseCase,
    private val filterQuests: pe.greenminds.ecomind.quests.application.FilterQuestsUseCase,
    private val offlineQuests: OfflineQuestStore
) : ViewModel() {

    companion object {
        private const val TYPING_PAUSE_MILLIS = 300L
    }

    private val route = savedStateHandle.toRoute<QuestSearchRoute>()
    private var routeQuestType = if (savedStateHandle.get<String>("questFilters") == null)
        route.questType?.let(QuestType::valueOf) else null
    private val initialFilters = pe.greenminds.ecomind.quests.application.QuestFilters(
        categories = setOfNotNull(route.category),
        types = when (route.questType) {
            "COLLABORATIVE" -> setOf("COLLABORATIVE")
            "MINIGAME" -> setOf("MINIGAME")
            "ACTIVITIES" -> setOf("CHECKBOX", "WRITE")
            else -> emptySet()
        }
    )

    private val _state = MutableStateFlow(QuestSearchUiState(
        query = savedStateHandle["searchQuery"] ?: "",
        focusSearch = route.focusSearch,
        filters = savedStateHandle.get<String>("questFilters")?.let {
            com.google.gson.Gson().fromJson(it, pe.greenminds.ecomind.quests.application.QuestFilters::class.java)
        } ?: initialFilters
    ))
    val state: StateFlow<QuestSearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun setOnline(online: Boolean) {
        _state.update {
            if (it.online == online) it else it.copy(online = online, quests = emptyList(), hasError = false, isLoading = true)
        }
        searchNow()
    }

    fun onQueryChange(query: String) {
        savedStateHandle["searchQuery"] = query
        _state.update { it.copy(query = query) }
        search(waitForTyping = true)
    }

    fun searchNow() {
        search(waitForTyping = false)
    }

    fun applyFilters(filters: pe.greenminds.ecomind.quests.application.QuestFilters) {
        routeQuestType = null
        savedStateHandle["questFilters"] = com.google.gson.Gson().toJson(filters)
        _state.update { it.copy(filters = filters, focusSearch = false) }
        searchNow()
    }

    private fun search(waitForTyping: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (waitForTyping) delay(TYPING_PAUSE_MILLIS)
            _state.update { it.copy(isLoading = true, hasError = false) }
            val request = _state.value
            val questType = routeQuestType
            if (!request.online) {
                val account = offlineQuests.session()
                if (account == null) {
                    showResults(Result.success(emptyList()))
                    return@launch
                }
                offlineQuests.observe(account.accountId).collect { saved ->
                    val query = request.query.trim()
                    val matching = saved.filter {
                        val quest = it.execution.quest
                        it.isAvailableOffline && (questType == null || quest.type == questType) &&
                            quest.title.contains(query, ignoreCase = true)
                    }.associateBy { it.execution.quest.id }
                    showResults(filterQuests(matching.values.map { it.execution.quest }, request.filters) { id ->
                        matching.getValue(id).execution.activities
                    })
                }
                return@launch
            }

            val results = searchQuests(
                query = request.query,
                category = null,
                questType = questType
            )
                .fold(
                    onSuccess = { filterQuests(it, request.filters) },
                    onFailure = { Result.failure(it) }
                )
            showResults(results)
        }
    }

    private fun showResults(results: Result<List<Quest>>) {
        _state.update {
            it.copy(quests = results.getOrDefault(emptyList()), isLoading = false, hasError = results.isFailure)
        }
    }
}

