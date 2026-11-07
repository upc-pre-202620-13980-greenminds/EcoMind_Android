package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.FamilyQuestExecution
import pe.greenminds.ecomind.quests.application.service.FamilyPlanService
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanUiError
import pe.greenminds.ecomind.quests.presentation.states.FamilyQuestExecutionUiState
import javax.inject.Inject

@HiltViewModel
class FamilyQuestExecutionViewModel @Inject constructor(private val service: FamilyPlanService) : ViewModel() {
    private val mutable = MutableStateFlow(FamilyQuestExecutionUiState())
    val state = mutable.asStateFlow()
    private var selection: Pair<Long, Long>? = null
    private var job: Job? = null

    fun open(planId: Long, itemId: Long) {
        job?.cancel()
        selection = planId to itemId
        mutable.value = FamilyQuestExecutionUiState()
        refresh()
    }

    fun refresh() {
        val (planId, itemId) = selection ?: return
        perform { service.getExecution(planId, itemId).getOrThrow() }
    }

    fun checkActivity(activityUserId: Long, checked: Boolean) {
        val (planId, itemId) = selection ?: return
        perform { service.checkActivity(planId, itemId, activityUserId, checked).getOrThrow() }
    }

    private fun perform(action: suspend () -> FamilyQuestExecution) {
        if (mutable.value.busy) return
        mutable.update { it.copy(busy = true, error = null) }
        job = viewModelScope.launch {
            try {
                val execution = action()
                mutable.update { it.copy(execution = execution, busy = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val current = selection?.let { (planId, itemId) -> service.getExecution(planId, itemId).getOrNull() }
                mutable.value = FamilyQuestExecutionUiState(current, false, FamilyPlanUiError.from(exception))
            }
        }
    }
}
