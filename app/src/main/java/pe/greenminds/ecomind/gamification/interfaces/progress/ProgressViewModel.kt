package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.*
import pe.greenminds.ecomind.gamification.domain.model.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(private val getOverview: GetGamificationOverviewUseCase) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<GamificationOverview>>(LoadState.Loading)
    val state = _state.asStateFlow()
    private var job: Job? = null
    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = LoadState.Loading
            try {
                getOverview().onSuccess { _state.value = LoadState.Loaded(it) }
                    .onFailure { if (it is CancellationException) throw it
                        _state.value = LoadState.Failed(it is AchievementSessionRequiredException) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = LoadState.Failed(e is AchievementSessionRequiredException) }
        }
    }
}
