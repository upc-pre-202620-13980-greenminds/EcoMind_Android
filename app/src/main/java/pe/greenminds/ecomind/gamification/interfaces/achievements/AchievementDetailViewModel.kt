package pe.greenminds.ecomind.gamification.interfaces.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.GetAchievementByIdUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementNotFoundException
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import javax.inject.Inject

@HiltViewModel
class AchievementDetailViewModel @Inject constructor(
    private val getAchievementById: GetAchievementByIdUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AchievementDetailUiState>(AchievementDetailUiState.Loading)
    val uiState: StateFlow<AchievementDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadAchievementById(id: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = AchievementDetailUiState.Loading
            try {
                getAchievementById(id)
                    .onSuccess { detail -> _uiState.value = AchievementDetailUiState.Success(detail) }
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
        _uiState.value = when (exception) {
            is AchievementSessionRequiredException -> AchievementDetailUiState.SessionRequired
            is AchievementNotFoundException -> AchievementDetailUiState.NotFound
            else -> AchievementDetailUiState.Error
        }
    }
}
