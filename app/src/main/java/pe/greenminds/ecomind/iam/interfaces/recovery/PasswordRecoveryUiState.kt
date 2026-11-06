package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.annotation.StringRes

data class RequestPasswordRecoveryUiState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    val isRequested: Boolean = false
)

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    val hasInvalidLink: Boolean = false,
    val isCompleted: Boolean = false,
    val isFinished: Boolean = false
)
