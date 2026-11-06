package pe.greenminds.ecomind.iam.interfaces.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun acknowledgeSignOut() {
        _state.value = SettingsUiState()
    }

    fun logout() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // The use case clears local credentials even when the request fails.
                logoutUseCase()
                _state.update { it.copy(isLoading = false, isSignedOut = true) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = R.string.error_logout_storage)
                }
            }
        }
    }
}
