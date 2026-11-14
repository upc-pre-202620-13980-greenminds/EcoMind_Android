package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.quests.application.usecase.GetProgressOverviewUseCase
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressOverview: GetProgressOverviewUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    init { loadProgress() }

    fun loadProgress() = load(showRefresh = false)
    fun refreshProgress() = load(showRefresh = true)

    private fun load(showRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = !it.hasLoaded, isRefreshing = showRefresh,
                isUpdating = it.hasLoaded, hasError = false) }
            try {
                getProgressOverview().onSuccess { overview ->
                    _state.value = ProgressUiState(
                        isLoading = false, hasLoaded = true, inProgress = overview.inProgress,
                        familyQuests = overview.familyQuests, finished = overview.finished, history = overview.history
                    )
                }.onFailure {
                    _state.update { it.copy(isLoading = false, isRefreshing = false, isUpdating = false, hasError = true) }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, isUpdating = false, hasError = true) }
            }
        }
    }
}
