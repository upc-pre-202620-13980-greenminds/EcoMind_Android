package pe.greenminds.ecomind.iam.interfaces.splash

enum class SplashDestination {
    SIGN_IN,
    MAIN_MENU
}

data class SplashUiState(
    // Null while the stored session is being checked
    val destination: SplashDestination? = null
)
