package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.ConfirmPasswordRecoveryUseCase
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.domain.services.PasswordPolicy
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val confirmPasswordRecoveryUseCase: ConfirmPasswordRecoveryUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()

    // Recovery credentials remain in memory and are never placed in saved state.
    private var recoveryToken: String? = null
    private var lastReceivedToken: String? = null
    private var sessionCleared = false

    fun setToken(token: String) {
        val normalizedToken = token.trim()
        if (lastReceivedToken == normalizedToken) return
        lastReceivedToken = normalizedToken
        recoveryToken = normalizedToken
        sessionCleared = false
        _state.value = ResetPasswordUiState(hasInvalidLink = normalizedToken.isEmpty())
    }

    fun onPasswordChange(password: String) {
        if (_state.value.isLoading || _state.value.isCompleted) return
        _state.update { it.copy(password = password, passwordError = null, formError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        if (_state.value.isLoading || _state.value.isCompleted) return
        _state.update { it.copy(confirmPassword = password, confirmPasswordError = null, formError = null) }
    }

    fun resetPassword() {
        val current = _state.value
        if (current.isLoading || current.isCompleted || current.hasInvalidLink) return
        val token = recoveryToken
        if (token.isNullOrBlank()) {
            _state.update { it.copy(hasInvalidLink = true, password = "", confirmPassword = "") }
            return
        }

        val passwordError = when {
            current.password.isEmpty() -> R.string.error_password_required
            !PasswordPolicy.isSatisfiedBy(current.password) -> R.string.error_password_policy
            else -> null
        }
        val confirmPasswordError = when {
            current.confirmPassword.isEmpty() -> R.string.recovery_confirm_password_required
            current.password != current.confirmPassword -> R.string.error_password_mismatch
            else -> null
        }
        _state.update {
            it.copy(passwordError = passwordError, confirmPasswordError = confirmPasswordError, formError = null)
        }
        if (passwordError != null || confirmPasswordError != null) return

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // A successful server reset must always clear the old local credentials,
            // even if the user leaves this screen or receives another recovery link.
            val (result, cleared) = withContext(NonCancellable) {
                val response = confirmPasswordRecoveryUseCase(token, current.password)
                response to if (response.isSuccess) clearStoredSession() else false
            }
            if (recoveryToken != token) return@launch
            result
                .onSuccess {
                    recoveryToken = null
                    sessionCleared = cleared
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = true,
                            password = "",
                            confirmPassword = "",
                            formError = if (cleared) null else R.string.recovery_session_clear_error
                        )
                    }
                }
                .onFailure { exception ->
                    when ((exception as? AuthException)?.error) {
                        AuthError.PASSWORD_RESET_TOKEN_INVALID -> _state.update {
                            it.copy(isLoading = false, hasInvalidLink = true, password = "", confirmPassword = "")
                        }
                        AuthError.PASSWORD_POLICY_VIOLATION -> _state.update {
                            it.copy(isLoading = false, passwordError = R.string.error_password_policy)
                        }
                        else -> _state.update {
                            it.copy(isLoading = false, formError = R.string.recovery_reset_error)
                        }
                    }
                }
        }
    }

    fun finish() {
        if (!_state.value.isCompleted || _state.value.isLoading) return
        if (sessionCleared) {
            _state.update { it.copy(isFinished = true) }
            return
        }
        _state.update { it.copy(isLoading = true, formError = null) }
        val receivedToken = lastReceivedToken
        viewModelScope.launch {
            val cleared = withContext(NonCancellable) { clearStoredSession() }
            if (receivedToken != lastReceivedToken || !_state.value.isCompleted) return@launch
            sessionCleared = cleared
            _state.update {
                it.copy(
                    isLoading = false,
                    isFinished = cleared,
                    formError = if (cleared) null else R.string.recovery_session_clear_error
                )
            }
        }
    }

    private suspend fun clearStoredSession(): Boolean {
        return try {
            sessionRepository.clearSession()
            true
        } catch (_: Exception) {
            false
        }
    }
}
