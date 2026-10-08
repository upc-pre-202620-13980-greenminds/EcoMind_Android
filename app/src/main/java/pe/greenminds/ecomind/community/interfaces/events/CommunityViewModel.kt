package pe.greenminds.ecomind.community.interfaces.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.community.application.GetCommunityEventsUseCase
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityEvents: GetCommunityEventsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        loadEvents()
    }

    // Only the main screen is built: there are no error or empty states yet,
    // so a failure leaves the list without events
    private fun loadEvents() {
        viewModelScope.launch {
            val events = getCommunityEvents().getOrDefault(emptyList())

            _state.update { currentState ->
                currentState.copy(isLoading = false, events = events)
            }
        }
    }
}
