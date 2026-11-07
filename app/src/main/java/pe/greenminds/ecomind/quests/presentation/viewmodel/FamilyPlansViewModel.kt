package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.service.FamilyPlanService
import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanUiError
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanEvent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import javax.inject.Inject

@HiltViewModel
class FamilyPlansViewModel @Inject constructor(private val service: FamilyPlanService) : ViewModel() {
    private val mutable = MutableStateFlow(FamilyPlansUiState())
    val state = mutable.asStateFlow()
    private val mutableEvents = MutableSharedFlow<FamilyPlanEvent>(extraBufferCapacity = 1)
    val events = mutableEvents.asSharedFlow()
    private var searchJob: Job? = null
    private var selectedPlanId: Long? = null

    fun refresh() = perform {
        val overview = service.getOverview().getOrThrow()
        mutable.update { it.copy(overview = overview) }
        selectedPlanId?.let { loadDetails(it) }
    }

    fun openPlan(planId: Long) = perform {
        selectedPlanId = planId
        mutable.update { it.copy(details = null) }
        loadDetails(planId)
    }

    fun openBuilder() = perform {
        val plan = service.ensureDraft().getOrThrow()
        selectedPlanId = plan.id
        loadDetails(plan.id)
        val overview = service.getOverview().getOrThrow()
        mutable.update { it.copy(overview = overview) }
        search()
        mutableEvents.emit(FamilyPlanEvent.DraftOpened(plan.id))
    }

    fun search(
        query: String = mutable.value.query,
        category: QuestCategory? = mutable.value.category,
        theme: QuestTheme? = mutable.value.theme
    ) {
        searchJob?.cancel()
        mutable.update { it.copy(query = query, category = category, theme = theme, catalog = emptyList(), searching = true, searchError = null) }
        searchJob = viewModelScope.launch {
            val result = service.searchQuests(query, category, theme)
            result.onSuccess { quests -> mutable.update { it.copy(catalog = quests, searching = false) } }
                .onFailure { error -> mutable.update { it.copy(searching = false, searchError = FamilyPlanUiError.from(error)) } }
        }
    }

    fun addQuest(questId: Long) = changePlan { id -> service.addQuest(id, questId).getOrThrow() }
    fun removeQuest(questId: Long) = changePlan { id -> service.removeQuest(id, questId).getOrThrow() }
    fun activate() = changePlan({ FamilyPlanEvent.Activated(it) }) { id -> service.activate(id).getOrThrow() }
    fun complete() = changePlan({ FamilyPlanEvent.Completed(it) }) { id -> service.complete(id).getOrThrow() }

    fun delete() = perform {
        val planId = requireNotNull(mutable.value.details).plan.id
        service.delete(planId).getOrThrow()
        selectedPlanId = null
        mutable.update { it.copy(details = null) }
        mutableEvents.emit(FamilyPlanEvent.Deleted(planId))
        val overview = service.getOverview().getOrThrow()
        mutable.update { it.copy(overview = overview) }
    }

    fun dismissError() { mutable.update { it.copy(error = null) } }

    private fun changePlan(
        event: ((Long) -> FamilyPlanEvent)? = null,
        action: suspend (Long) -> FamilyPlan
    ) = perform {
        val planId = requireNotNull(mutable.value.details).plan.id
        val updated = action(planId)
        mutable.update { current -> current.copy(details = current.details?.copy(plan = updated)) }
        event?.let { mutableEvents.emit(it(updated.id)) }
        loadDetails(updated.id)
        val overview = service.getOverview().getOrThrow()
        mutable.update { it.copy(overview = overview) }
    }

    private suspend fun loadDetails(planId: Long) {
        val details = service.getDetails(planId).getOrThrow()
        mutable.update { it.copy(details = details) }
    }

    private fun perform(action: suspend () -> Unit) {
        if (mutable.value.busy) return
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val overview = service.getOverview().getOrNull()
                val details = selectedPlanId?.let { service.getDetails(it).getOrNull() }
                mutable.update { it.copy(
                    overview = overview ?: it.overview, details = details,
                    error = FamilyPlanUiError.from(exception)
                ) }
            } finally {
                mutable.update { it.copy(busy = false) }
            }
        }
    }
}
