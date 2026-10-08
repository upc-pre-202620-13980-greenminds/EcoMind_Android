package pe.greenminds.ecomind.main

import androidx.annotation.StringRes

data class MainMenuUiState(
    val gemBalance: Int = 0,
    val ecopoints: Int = 0,
    val isLoading: Boolean = true,
    @StringRes val errorMessage: Int? = null
)
