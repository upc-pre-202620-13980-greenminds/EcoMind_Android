package pe.greenminds.ecomind.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainMenuUiState())
    val state: StateFlow<MainMenuUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoading = true, errorMessage = null)
            }

            getCurrentProfile()
                .onSuccess { profile ->
                    _state.update { currentState ->
                        currentState.copy(
                            gemBalance = profile.gemBalance,
                            ecopoints = profile.ecopoints,
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = R.string.error_profile_load
                        )
                    }
                }
        }
    }
}
