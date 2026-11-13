package pe.greenminds.ecomind.gamification.interfaces.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.CheckRankingAccessUseCase
import pe.greenminds.ecomind.gamification.application.GetRankingUseCase
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val checkRankingAccess: CheckRankingAccessUseCase,
    private val getRanking: GetRankingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var accessGranted = false

    fun onTypeSelected(type: RankingType) {
        _state.update { currentState ->
            currentState.copy(selectedType = type)
        }
        load()
    }

    fun onPeriodSelected(period: RankingPeriod) {
        _state.update { currentState ->
            currentState.copy(selectedPeriod = period)
        }
        load()
    }

    // Also used by "Try again"
    fun load() = load(quiet = false)

    fun onResume() = load(quiet = _state.value.ranking != null || _state.value.isLocked)

    fun refresh() {
        if (loadJob?.isActive != true) load(quiet = true)
    }

    private fun load(quiet: Boolean) {
        // Changing tab or period quickly leaves only the last request running
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoading = !quiet && !currentState.isLocked,
                    hasError = false, sessionRequired = false, refreshFailed = false)
            }

            // The rule is checked until it passes once; after that it is not asked again
            if (!accessGranted) {
                accessGranted = checkRankingAccess().getOrElse {
                    showError(it, quiet)
                    return@launch
                }
            }
            if (!accessGranted) {
                _state.update { currentState ->
                    currentState.copy(isLoading = false, isLocked = true)
                }
                return@launch
            }

            val current = _state.value
            getRanking(current.selectedType, current.selectedPeriod)
                .onSuccess { ranking ->
                    _state.update { currentState ->
                        currentState.copy(isLoading = false, isLocked = false, ranking = ranking, refreshFailed = false)
                    }
                }
                .onFailure { showError(it, quiet) }
        }
    }

    private fun showError(exception: Throwable, quiet: Boolean) {
        if (exception is CancellationException) throw exception
        if (quiet && exception !is AchievementSessionRequiredException &&
            (_state.value.ranking != null || _state.value.isLocked)) {
            _state.update { it.copy(isLoading = false, refreshFailed = true) }
            return
        }
        _state.update { currentState ->
            currentState.copy(isLoading = false, hasError = true, ranking = null, isLocked = false,
                sessionRequired = exception is AchievementSessionRequiredException)
        }
    }
}
