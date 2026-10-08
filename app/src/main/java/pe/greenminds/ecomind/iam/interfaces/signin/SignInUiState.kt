package pe.greenminds.ecomind.iam.interfaces.signin

import androidx.annotation.StringRes

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    // Errors are string resources so the screen shows them in the language of the device
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false
)
