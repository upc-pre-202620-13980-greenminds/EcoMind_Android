package pe.greenminds.ecomind.settings.domain.repositories

import kotlinx.coroutines.flow.Flow
import pe.greenminds.ecomind.settings.domain.model.*

interface SettingsRepository {
    fun observe(): Flow<AppPreferences>
    suspend fun setLanguage(value: AppLanguage): Result<Unit>
    suspend fun setTheme(value: AppTheme): Result<Unit>
    suspend fun setNotification(category: NotificationCategory, enabled: Boolean): Result<Unit>
}
