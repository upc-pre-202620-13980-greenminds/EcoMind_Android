package pe.greenminds.ecomind.quests.interfaces.detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.QuestExecutionUseCase
import pe.greenminds.ecomind.quests.domain.model.QuestExecution
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import javax.inject.Inject
@HiltViewModel
class QuestDetailViewModel @Inject constructor(private val execution: QuestExecutionUseCase) : ViewModel() {
    private val _state = MutableStateFlow(QuestDetailUiState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    fun load(id: Long) { if (!_state.value.busy) run { execution.load(id) } }
    fun start(id: Long) { if (!_state.value.busy) run { execution.start(id) } }
    fun check(id: Long, assignmentId: Long, done: Boolean) { if (!_state.value.busy) run { execution.check(id, assignmentId, done) } }
    fun finish(id: Long) { if (!_state.value.busy) run { execution.finish(id) } }
    private fun run(action: suspend () -> Result<QuestExecution>) {
        job?.cancel()
        _state.value = _state.value.copy(busy = true, error = false)
        job = viewModelScope.launch {
            try {
                val data = action().getOrThrow()
                _state.value = QuestDetailUiState(loading = false, execution = data)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = _state.value.copy(loading = false, busy = false, error = true, sessionRequired = e is AchievementSessionRequiredException) }
        }
    }
}
