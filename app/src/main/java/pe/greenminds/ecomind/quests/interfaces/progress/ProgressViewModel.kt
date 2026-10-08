package pe.greenminds.ecomind.quests.interfaces.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.GetProgressOverviewUseCase
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressOverview: GetProgressOverviewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        loadProgress()
    }

    // Only the main screen is built: there are no error or empty states yet,
    // so a failure leaves the cards without rows
    private fun loadProgress() {
        viewModelScope.launch {
            val overview = getProgressOverview().getOrNull()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    inProgress = overview?.inProgress ?: emptyList(),
                    familyQuests = overview?.familyQuests ?: emptyList(),
                    finished = overview?.finished ?: emptyList()
                )
            }
        }
    }
}
