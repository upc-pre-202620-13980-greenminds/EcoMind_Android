package pe.greenminds.ecomind.notifications.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.domain.repositories.PushNotifier
import pe.greenminds.ecomind.notifications.infrastructure.implementation.RemoteNotificationRepository
import pe.greenminds.ecomind.notifications.infrastructure.local.SystemPushNotifier

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsRepositoryModule {

    @Binds
    fun bindNotificationRepository(impl: RemoteNotificationRepository): NotificationRepository

    // The web services have no push yet, so the phone shows the alerts itself
    @Binds
    fun bindPushNotifier(impl: SystemPushNotifier): PushNotifier
}
