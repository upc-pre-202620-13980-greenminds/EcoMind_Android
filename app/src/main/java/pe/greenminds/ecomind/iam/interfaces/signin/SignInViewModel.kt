package pe.greenminds.ecomind.iam.interfaces.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.SignInUseCase
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { currentState ->
            currentState.copy(email = email, emailError = null, formError = null)
        }
    }

    fun onPasswordChange(password: String) {
        _state.update { currentState ->
            currentState.copy(password = password, passwordError = null, formError = null)
        }
    }

    fun signIn() {
        // Ignores a second tap while the first request is running
        if (_state.value.isLoading) return
        if (!validate()) return

        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoading = true, formError = null)
            }

            signInUseCase(_state.value.email, _state.value.password)
                .onSuccess {
                    _state.update { currentState ->
                        currentState.copy(isLoading = false, isAuthenticated = true)
                    }
                }
                .onFailure { exception ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            formError = toMessage(exception)
                        )
                    }
                }
        }
    }

    private fun validate(): Boolean {
        val email = EmailAddress.normalize(_state.value.email)
        val password = _state.value.password

        val emailError = when {
            email.isEmpty() -> R.string.error_email_required
            !EmailAddress.isValid(email) -> R.string.error_email_invalid
            else -> null
        }
        val passwordError = if (password.isEmpty()) R.string.error_password_required else null

        _state.update { currentState ->
            currentState.copy(emailError = emailError, passwordError = passwordError)
        }
        return emailError == null && passwordError == null
    }

    private fun toMessage(exception: Throwable): Int {
        val error = (exception as? AuthException)?.error ?: AuthError.UNKNOWN
        return when (error) {
            AuthError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
            else -> R.string.error_unknown
        }
    }
}
