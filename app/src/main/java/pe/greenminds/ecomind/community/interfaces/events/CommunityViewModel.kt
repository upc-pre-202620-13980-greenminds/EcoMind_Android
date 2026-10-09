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
import pe.greenminds.ecomind.community.interfaces.sections.CommunitySection
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

    fun selectSection(section: CommunitySection) {
        _state.update { currentState ->
            currentState.copy(selectedSection = section)
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val events = getCommunityEvents().getOrDefault(emptyList())

            _state.update { currentState ->
                currentState.copy(isLoading = false, events = events)
            }
        }
    }
}
