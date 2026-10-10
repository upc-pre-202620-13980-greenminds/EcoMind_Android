package pe.greenminds.ecomind.settings.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.settings.domain.repositories.SettingsRepository
import pe.greenminds.ecomind.settings.infrastructure.local.LocalSettingsRepository
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.infrastructure.local.LocalNotificationRepository

@Module
@InstallIn(SingletonComponent::class)
interface ExperienceRepositoryModule {
    @Binds fun settings(impl: LocalSettingsRepository): SettingsRepository
    @Binds fun notifications(impl: LocalNotificationRepository): NotificationRepository
}
