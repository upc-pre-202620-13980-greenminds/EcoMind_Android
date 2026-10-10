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
import pe.greenminds.ecomind.gamification.application.GetRewardHistoryUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistory: GetRewardHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun loadHistory(period: RankingPeriod = RankingPeriod.ALL_TIME) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = HistoryUiState()
            try {
                getHistory(period)
                    .onSuccess { result ->
                        _state.update { currentState ->
                            currentState.copy(isLoading = false, rewards = result)
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
