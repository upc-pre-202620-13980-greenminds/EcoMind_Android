package pe.greenminds.ecomind.notifications.domain.repositories

import pe.greenminds.ecomind.notifications.domain.model.Notification

// Alerts of the phone (TS-008). The web services do not send push yet, so the app shows
// the new notifications of the inbox while it is open.
interface PushNotifier {

    // Shows the unread notifications that arrived after the last one already shown
    suspend fun showNew(accountId: Long, notifications: List<Notification>)

    // The user already saw them in the inbox, so they are not shown as alerts later
    suspend fun markAsShown(accountId: Long, notifications: List<Notification>)
}
