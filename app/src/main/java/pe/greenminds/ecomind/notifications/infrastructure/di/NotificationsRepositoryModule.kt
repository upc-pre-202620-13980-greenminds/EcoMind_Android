package pe.greenminds.ecomind.notifications.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.infrastructure.implementation.RemoteNotificationRepository

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsRepositoryModule {

    @Binds
    fun bindNotificationRepository(impl: RemoteNotificationRepository): NotificationRepository
}
