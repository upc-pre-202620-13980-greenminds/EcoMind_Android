package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.GetGamificationOverviewUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgress: GetGamificationOverviewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun loadProgress() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = ProgressUiState()
            try {
                getProgress()
                    .onSuccess { result ->
                        _state.update { currentState ->
                            currentState.copy(isLoading = false, overview = result)
                        }
                    }
                    .onFailure { exception -> showError(exception) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(exception)
            }
        }
    }

    private fun showError(exception: Throwable) {
        if (exception is CancellationException) throw exception
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                hasError = true,
                sessionRequired = exception is AchievementSessionRequiredException
            )
        }
    }
}
