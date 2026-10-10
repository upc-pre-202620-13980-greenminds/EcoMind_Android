package pe.greenminds.ecomind.settings.interfaces.preferences

import pe.greenminds.ecomind.settings.domain.model.AppPreferences

data class PreferencesUiState(val preferences: AppPreferences = AppPreferences(), val isLoading: Boolean = true, val isSaving: Boolean = false, val hasError: Boolean = false)
