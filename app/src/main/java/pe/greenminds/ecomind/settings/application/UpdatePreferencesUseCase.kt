package pe.greenminds.ecomind.settings.application

import pe.greenminds.ecomind.settings.domain.model.*
import pe.greenminds.ecomind.settings.domain.repositories.SettingsRepository
import javax.inject.Inject

class UpdatePreferencesUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend fun language(value: AppLanguage) = repository.setLanguage(value)
    suspend fun theme(value: AppTheme) = repository.setTheme(value)
    suspend fun notification(category: NotificationCategory, enabled: Boolean) = repository.setNotification(category, enabled)
}
