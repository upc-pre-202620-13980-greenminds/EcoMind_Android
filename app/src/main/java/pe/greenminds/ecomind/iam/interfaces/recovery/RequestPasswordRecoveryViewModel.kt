package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.RequestPasswordRecoveryUseCase
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import javax.inject.Inject

@HiltViewModel
class RequestPasswordRecoveryViewModel @Inject constructor(
    private val requestPasswordRecoveryUseCase: RequestPasswordRecoveryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RequestPasswordRecoveryUiState())
    val state: StateFlow<RequestPasswordRecoveryUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(email = email, emailError = null, formError = null, isRequested = false) }
    }

    fun requestRecovery() {
        if (_state.value.isLoading) return
        val email = EmailAddress.normalize(_state.value.email)
        val error = when {
            email.isEmpty() -> R.string.error_email_required
            !EmailAddress.isValid(email) -> R.string.error_email_invalid
            else -> null
        }
        _state.update { it.copy(email = email, emailError = error, formError = null) }
        if (error != null) return

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            requestPasswordRecoveryUseCase(email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isRequested = true) }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, formError = R.string.recovery_request_error)
                    }
                }
        }
    }
}
