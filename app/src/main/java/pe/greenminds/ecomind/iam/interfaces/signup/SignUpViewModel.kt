package pe.greenminds.ecomind.iam.interfaces.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.SignUpUseCase
import pe.greenminds.ecomind.iam.application.VerifyEmailUseCase
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.domain.model.PersonName
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.model.VerificationCode
import pe.greenminds.ecomind.iam.domain.services.PasswordPolicy
import javax.inject.Inject

// One ViewModel for the form and the code: resending the code needs the data of the form
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null, formError = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, formError = null) }
    }

    fun onRoleSelected(role: SocialRole) {
        _state.update { it.copy(role = role, roleError = null, formError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, formError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                formError = null
            )
        }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _state.update { it.copy(termsAccepted = accepted, termsError = null, formError = null) }
    }

    fun onCodeChange(code: String) {
        // Only digits are kept, up to the length of the code
        val digits = code.filter { it.isDigit() }.take(VerificationCode.LENGTH)
        _state.update {
            it.copy(code = digits, codeError = null, codeResent = false, formError = null)
        }
    }

    fun submitRegistration() {
        if (_state.value.isLoading) return
        if (!validateForm()) return

        requestCode(isResend = false)
    }

    fun resendCode() {
        if (_state.value.isLoading) return

        requestCode(isResend = true)
    }

    fun verifyCode() {
        val current = _state.value
        if (current.isLoading) return

        if (!VerificationCode.isValid(current.code)) {
            _state.update { it.copy(codeError = R.string.error_code_format) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, formError = null, codeResent = false) }

            verifyEmailUseCase(current.email, current.code)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isVerified = true) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, formError = toMessage(exception)) }
                }
        }
    }

    fun backToForm() {
        _state.update {
            it.copy(
                step = SignUpStep.FORM,
                code = "",
                codeError = null,
                codeResent = false,
                formError = null
            )
        }
    }

    // Sending the registration again is how the web services send a new code
    private fun requestCode(isResend: Boolean) {
        val current = _state.value
        val role = current.role ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, formError = null, codeResent = false) }

            signUpUseCase(current.name, current.email, current.password, role)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = SignUpStep.CODE,
                            code = "",
                            codeError = null,
                            codeResent = isResend
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, formError = toMessage(exception)) }
                }
        }
    }

    private fun validateForm(): Boolean {
        val current = _state.value
        val email = EmailAddress.normalize(current.email)

        val nameError = if (PersonName.isValid(current.name)) null else R.string.error_name_invalid
        val emailError = when {
            email.isEmpty() -> R.string.error_email_required
            !EmailAddress.isValid(email) -> R.string.error_email_invalid
            else -> null
        }
        val roleError = if (current.role == null) R.string.error_role_required else null
        val passwordError = if (PasswordPolicy.isSatisfiedBy(current.password)) {
            null
        } else {
            R.string.error_password_policy
        }
        val confirmPasswordError = if (current.confirmPassword == current.password) {
            null
        } else {
            R.string.error_password_mismatch
        }
        val termsError = if (current.termsAccepted) null else R.string.error_terms_required

        _state.update {
            it.copy(
                nameError = nameError,
                emailError = emailError,
                roleError = roleError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                termsError = termsError
            )
        }

        return listOf(
            nameError,
            emailError,
            roleError,
            passwordError,
            confirmPasswordError,
            termsError
        ).all { it == null }
    }

    private fun toMessage(exception: Throwable): Int {
        val error = (exception as? AuthException)?.error ?: AuthError.UNKNOWN
        return when (error) {
            AuthError.EMAIL_CONFLICT -> R.string.error_email_conflict
            AuthError.VERIFICATION_CODE_INVALID -> R.string.error_code_invalid
            else -> R.string.error_unknown
        }
    }
}
