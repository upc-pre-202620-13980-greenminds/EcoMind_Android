package pe.greenminds.ecomind.iam.interfaces.settings

import androidx.annotation.StringRes

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    @param:StringRes val errorMessage: Int? = null
)
