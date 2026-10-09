package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.*
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.greenminds.ecomind.quests.application.service.*
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute

enum class QuestStage { DETAIL, STARTING, ACTIVITIES, FINISHED }
data class QuestExecutionState(val execution: QuestExecution? = null, val stage: QuestStage = QuestStage.DETAIL,
    val busy: Boolean = false, val error: Boolean = false)

@HiltViewModel
class QuestExecutionViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val service: QuestExecutionService) : ViewModel() {
    private val questId = savedStateHandle.toRoute<QuestDetailRoute>().questId
    private val mutable = MutableStateFlow(QuestExecutionState())
    val state = mutable.asStateFlow()
    init { reload() }
    fun reload() = perform { copy(execution = service.load(questId)) }
    fun start() = perform {
        val execution = service.start(requireNotNull(execution))
        copy(execution = execution, stage = QuestStage.STARTING)
    }
    fun animationEnded() { if (mutable.value.stage == QuestStage.STARTING) mutable.update { it.copy(stage = QuestStage.ACTIVITIES) } }
    fun check(id: Long, checked: Boolean) = perform { copy(execution = service.check(requireNotNull(execution), id, checked)) }
    fun finish() = perform { copy(execution = service.finish(requireNotNull(execution)), stage = QuestStage.FINISHED) }
    private fun perform(action: suspend QuestExecutionState.() -> QuestExecutionState) {
        if (mutable.value.busy) return
        mutable.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            try { mutable.value = action(mutable.value).copy(busy = false, error = false) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(busy = false, error = true) } }
        }
    }
}
