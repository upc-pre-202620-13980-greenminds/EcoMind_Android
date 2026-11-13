package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.application.usecase.CreateCommunityGoalUseCase
import pe.greenminds.ecomind.community.application.usecase.GetCommunityGoalPanelUseCase
import pe.greenminds.ecomind.community.application.usecase.JoinCommunityGoalUseCase
import pe.greenminds.ecomind.community.presentation.states.CommunityGoalUiState

@HiltViewModel
class CommunityGoalViewModel @Inject constructor(
    private val getPanel: GetCommunityGoalPanelUseCase,
    private val createGoal: CreateCommunityGoalUseCase,
    private val joinGoal: JoinCommunityGoalUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityGoalUiState())
    val state = _state.asStateFlow()

    private var job: Job? = null
    private var generation = 0L

    fun bind(communityId: Long) {
        if (_state.value.communityId == communityId) return
        clear()
        _state.value = CommunityGoalUiState(communityId = communityId)
        refresh()
    }

    fun clear() {
        generation++
        job?.cancel()
        _state.value = CommunityGoalUiState()
    }

    fun refresh() {
        val communityId = _state.value.communityId ?: return
        if (_state.value.saving) return

        job?.cancel()
        val version = ++generation

        _state.update {
            it.copy(loading = true, error = null, errorRes = null)
        }

        job = viewModelScope.launch {
            val result = getPanel(communityId)
            if (version != generation) return@launch

            result.fold(
                onSuccess = { panel ->
                    _state.update {
                        it.copy(panel = panel, loading = false)
                    }
                },
                onFailure = ::showError
            )
        }
    }

    fun toggleExpanded() {
        _state.update { it.copy(expanded = !it.expanded) }
    }

    fun openCreate() {
        val panel = _state.value.panel ?: return
        if (!panel.canCreate || panel.otherGoalActive) return
        _state.update {
            it.copy(showCreate = true, error = null, errorRes = null)
        }
    }

    fun closeCreate() {
        if (_state.value.saving) return
        _state.update { it.copy(showCreate = false) }
    }

    fun selectTopic(topic: String) {
        if (_state.value.saving) return
        if (topic !in setOf("water", "energy", "recycle")) return
        _state.update { it.copy(topic = topic) }
    }

    fun create() {
        val current = _state.value
        val communityId = current.communityId ?: return
        val panel = current.panel ?: return
        if (!panel.canCreate || panel.otherGoalActive) return

        mutate {
            createGoal(communityId, current.topic)
        }
    }

    fun join() {
        val panel = _state.value.panel ?: return
        val goal = panel.goal ?: return
        if (panel.joined || panel.otherGoalActive) return

        mutate {
            joinGoal(goal.id)
        }
    }

    private fun mutate(operation: suspend () -> Result<Unit>) {
        val communityId = _state.value.communityId ?: return
        if (_state.value.saving || _state.value.loading) return

        job?.cancel()
        val version = ++generation

        _state.update {
            it.copy(saving = true, error = null, errorRes = null)
        }

        job = viewModelScope.launch {
            val result = operation()
            if (version != generation) return@launch

            val failure = result.exceptionOrNull()
            if (failure != null) {
                _state.update { it.copy(panel = null) }
                showError(failure)
                return@launch
            }

            _state.update {
                it.copy(showCreate = false, expanded = true)
            }

            val refreshed = getPanel(communityId)
            if (version != generation) return@launch

            refreshed.fold(
                onSuccess = { panel ->
                    _state.update {
                        it.copy(panel = panel, saving = false, loading = false)
                    }
                },
                onFailure = {
                    _state.update { state -> state.copy(panel = null) }
                    showError(it)
                }
            )
        }
    }

    private fun showError(error: Throwable) {
        _state.update {
            it.copy(
                loading = false,
                saving = false,
                error = error.message,
                errorRes = if (error.message == null) {
                    R.string.goal_flow_error
                } else {
                    null
                }
            )
        }
    }
}