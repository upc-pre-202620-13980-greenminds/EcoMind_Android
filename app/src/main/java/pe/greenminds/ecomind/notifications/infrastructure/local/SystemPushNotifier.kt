package pe.greenminds.ecomind.notifications.infrastructure.local

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.repositories.PushNotifier
import javax.inject.Inject

// Its own file: it only keeps the id of the last notification shown as an alert
private val Context.pushDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "push_notifications"
)

// Uses NotificationManagerCompat of androidx.core, so it needs no new library
class SystemPushNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : PushNotifier {

    companion object {
        private const val CHANNEL_ID = "ecomind_notifications"
    }

    override suspend fun showNew(accountId: Long, notifications: List<Notification>) {
        val newest = notifications.maxOfOrNull { it.id } ?: return
        val lastShown = context.pushDataStore.data.first()[keyOf(accountId)]

        // The first time nothing is shown, so an old inbox does not fill the phone with alerts
        if (lastShown != null) {
            notifications
                .filter { !it.isRead && it.id > lastShown }
                .forEach { post(it) }
        }
        if (lastShown == null || newest > lastShown) {
            saveLastShown(accountId, newest)
        }
    }

    override suspend fun markAsShown(accountId: Long, notifications: List<Notification>) {
        val newest = notifications.maxOfOrNull { it.id } ?: return
        val lastShown = context.pushDataStore.data.first()[keyOf(accountId)]
        if (lastShown == null || newest > lastShown) {
            saveLastShown(accountId, newest)
        }
    }

    private fun post(notification: Notification) {
        // From Android 13 the user has to allow the alerts; without it nothing is shown
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.notifications_channel_name))
                .setDescription(context.getString(R.string.notifications_channel_description))
                .build()
        )

        // Tapping the alert opens the app where it was
        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = openApp?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val alert = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(notification.id.toInt(), alert)
        } catch (exception: SecurityException) {
            // The permission was removed while the app was open
        }
    }

    private suspend fun saveLastShown(accountId: Long, notificationId: Long) {
        context.pushDataStore.edit { preferences ->
            preferences[keyOf(accountId)] = notificationId
        }
    }

    // One value per account, so two users on the same phone do not mix their alerts
    private fun keyOf(accountId: Long) = longPreferencesKey("last_shown_$accountId")
}
