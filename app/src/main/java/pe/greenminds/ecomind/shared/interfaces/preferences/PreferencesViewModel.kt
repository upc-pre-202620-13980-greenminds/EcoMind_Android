package pe.greenminds.ecomind.shared.interfaces.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.application.GetPreferencesUseCase
import pe.greenminds.ecomind.shared.application.SetLanguageUseCase
import pe.greenminds.ecomind.shared.application.SetNotificationCategoryUseCase
import pe.greenminds.ecomind.shared.application.SetThemeUseCase
import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import javax.inject.Inject

// Used by MainActivity for the theme and by the screens of Settings (frames 69, 70, 73 and 74)
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    getPreferences: GetPreferencesUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val setTheme: SetThemeUseCase,
    private val setNotificationCategory: SetNotificationCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PreferencesUiState())
    val state: StateFlow<PreferencesUiState> = _state.asStateFlow()

    init {
        // The stored values arrive here every time they change, from this screen or another
        viewModelScope.launch {
            getPreferences().collect { preferences ->
                _state.update {
                    it.copy(
                        language = preferences.language,
                        theme = preferences.theme,
                        enabledCategories = preferences.enabledCategories
                    )
                }
            }
        }
    }

    // The screen is recreated with the new texts, so nothing else has to be updated
    fun onLanguageSelected(language: AppLanguage) {
        _state.update { it.copy(language = language, saveError = null) }
        setLanguage(language)
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            _state.update { it.copy(saveError = null) }
            setTheme(theme).onFailure {
                _state.update { it.copy(saveError = R.string.preferences_save_failed) }
            }
        }
    }

    fun onCategoryChanged(category: NotificationCategory, enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(saveError = null) }
            setNotificationCategory(category, enabled).onFailure {
                _state.update { it.copy(saveError = R.string.preferences_save_failed) }
            }
        }
    }
}
