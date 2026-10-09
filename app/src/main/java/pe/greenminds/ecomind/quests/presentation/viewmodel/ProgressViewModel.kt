package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.usecase.GetProgressOverviewUseCase
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressOverview: GetProgressOverviewUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state = _state.asStateFlow()
    init { loadProgress() }

    fun loadProgress() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, hasError = false) }
            getProgressOverview().onSuccess { overview ->
                _state.value = ProgressUiState(
                    isLoading = false, inProgress = overview.inProgress,
                    familyQuests = overview.familyQuests, finished = overview.finished
                )
            }.onFailure {
                _state.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }
}
