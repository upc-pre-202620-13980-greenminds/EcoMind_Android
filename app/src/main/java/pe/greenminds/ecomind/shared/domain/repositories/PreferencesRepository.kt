package pe.greenminds.ecomind.shared.domain.repositories

import kotlinx.coroutines.flow.Flow
import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory

interface PreferencesRepository {

    fun getTheme(): Flow<AppTheme>

    suspend fun setTheme(theme: AppTheme): Result<Unit>

    fun getEnabledCategories(): Flow<Set<NotificationCategory>>

    suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean): Result<Unit>

    // Android keeps the language, so it is read and written right away
    fun getLanguage(): AppLanguage

    fun setLanguage(language: AppLanguage)
}
