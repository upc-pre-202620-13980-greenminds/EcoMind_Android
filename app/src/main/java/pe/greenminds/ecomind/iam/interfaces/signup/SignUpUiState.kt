package pe.greenminds.ecomind.iam.interfaces.signup

import androidx.annotation.StringRes
import pe.greenminds.ecomind.iam.domain.model.SocialRole

enum class SignUpStep {
    FORM,
    CODE
}

data class SignUpUiState(
    val step: SignUpStep = SignUpStep.FORM,

    // Form
    val name: String = "",
    val email: String = "",
    val role: SocialRole? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val roleError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val termsError: Int? = null,

    // Code
    val code: String = "",
    @StringRes val codeError: Int? = null,
    val codeResent: Boolean = false,

    @StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false
)
