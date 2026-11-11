package pe.greenminds.ecomind.shared.interfaces.preferences

import androidx.annotation.StringRes
import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory

data class PreferencesUiState(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val theme: AppTheme = AppTheme.LIGHT,
    val enabledCategories: Set<NotificationCategory> = NotificationCategory.entries.toSet(),
    // Shown when a change could not be saved; the option goes back to its stored value
    @StringRes val saveError: Int? = null
)
