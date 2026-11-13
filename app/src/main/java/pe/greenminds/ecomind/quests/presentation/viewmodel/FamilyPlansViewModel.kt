package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import pe.greenminds.ecomind.quests.application.service.FamilyPlanService
import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.application.FilterQuestsUseCase
import pe.greenminds.ecomind.quests.application.QuestFilters
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanUiError
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanEvent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.gamification.interfaces.acl.GamificationContextFacade
import javax.inject.Inject

@HiltViewModel
class FamilyPlansViewModel @Inject constructor(
    private val service: FamilyPlanService,
    private val filterQuests: FilterQuestsUseCase,
    private val gamification: GamificationContextFacade
) : ViewModel() {
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
        mutable.update { it.copy(details = it.details?.takeIf { details -> details.plan.id == planId }) }
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

    fun openEditor(planId: Long) = perform {
        selectedPlanId = planId
        mutable.update { it.copy(details = it.details?.takeIf { details -> details.plan.id == planId }) }
        loadDetails(planId)
        search()
    }

    fun search(
        query: String = mutable.value.query,
        filters: QuestFilters = mutable.value.filters
    ) = search(query, filters, waitForTyping = false)

    fun onQueryChange(query: String) = search(query, mutable.value.filters, waitForTyping = true)

    private fun search(query: String, filters: QuestFilters, waitForTyping: Boolean) {
        searchJob?.cancel()
        mutable.update { it.copy(query = query, filters = filters, searchError = null) }
        searchJob = viewModelScope.launch {
            if (waitForTyping) delay(300L)
            mutable.update { it.copy(searching = true) }
            val result = service.searchQuests(query).fold(
                onSuccess = { filterQuests(it, filters) },
                onFailure = { Result.failure(it) }
            )
            result.onSuccess { quests -> mutable.update { it.copy(catalog = quests, searching = false) } }
                .onFailure { error -> mutable.update { it.copy(searching = false, searchError = FamilyPlanUiError.from(error)) } }
        }
    }

    fun addQuest(questId: Long) = changePlan { id -> service.addQuest(id, questId).getOrThrow() }
    fun removeQuest(questId: Long) = changePlan { id -> service.removeQuest(id, questId).getOrThrow() }
    fun moveQuest(questId: Long, direction: Int) = changePlan { id -> service.moveQuest(id, questId, direction).getOrThrow() }
    fun save() = changePlan({ FamilyPlanEvent.Saved(it) }) { id -> service.saveDraft(id).getOrThrow() }
    fun activate() = changePlan({ FamilyPlanEvent.Activated(it) }) { id -> service.activate(id).getOrThrow() }
    fun complete() = perform {
        val plan = requireNotNull(mutable.value.details).plan
        val previous = withTimeoutOrNull(5_000) {
            gamification.getFamilyAchievements(plan.familyId).getOrNull()
        }
        val completed = service.complete(plan.id).getOrThrow()
        mutable.update { current -> current.copy(details = current.details?.copy(plan = completed)) }
        // Award reads must never turn a committed completion into a failed completion.
        val achievements = if (previous == null) emptyList() else {
            val knownIds = previous.map { it.awardId }.toSet()
            withTimeoutOrNull(5_000) { gamification.getFamilyAchievements(plan.familyId).getOrNull() }.orEmpty()
                .filter { it.awardId !in knownIds }.map { it.name }
        }
        mutable.update { it.copy(completedAchievements = achievements) }
        mutableEvents.emit(FamilyPlanEvent.Completed(completed.id))
        withTimeoutOrNull(5_000) { service.getDetails(completed.id) }
            ?.onSuccess { details -> mutable.update { it.copy(details = details) } }
        withTimeoutOrNull(5_000) { service.getOverview() }
            ?.onSuccess { overview -> mutable.update { it.copy(overview = overview) } }
    }

    fun delete() { mutable.value.details?.plan?.id?.let(::deletePlan) }

    fun deletePlan(planId: Long) = perform {
        service.delete(planId).getOrThrow()
        if (selectedPlanId == planId) {
            selectedPlanId = null
            mutable.update { it.copy(details = null) }
        }
        mutableEvents.emit(FamilyPlanEvent.Deleted(planId))
        val overview = service.getOverview().getOrThrow()
        mutable.update { it.copy(overview = overview) }
    }

    fun dismissError() { mutable.update { it.copy(error = null) } }

    fun dismissCompletion() { mutable.update { it.copy(completedAchievements = null) } }

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
                val noFamily = overview != null && overview.membership == null
                if (noFamily) selectedPlanId = null
                mutable.update { it.copy(
                    overview = overview ?: it.overview,
                    details = if (noFamily) null else details ?: it.details,
                    error = FamilyPlanUiError.from(exception)
                ) }
            } finally {
                mutable.update { it.copy(busy = false) }
            }
        }
    }
}
