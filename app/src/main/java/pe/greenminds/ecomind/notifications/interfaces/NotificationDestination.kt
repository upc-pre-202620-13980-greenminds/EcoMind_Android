package pe.greenminds.ecomind.notifications.interfaces

import pe.greenminds.ecomind.notifications.domain.model.NotificationType

// Screens that already exist and show what a notification talks about. The web services do not
// send the id of the community, event or material, so a tap opens the section, not the item.
enum class NotificationDestination {
    MEDALS,
    COMMUNITY,
    FAVORITES,
    PROGRESS_REWARDS
}

// Null means there is no screen for it yet, so the tap only marks it as read
fun destinationFor(type: NotificationType): NotificationDestination? = when (type) {
    NotificationType.ACHIEVEMENT_UNLOCKED -> NotificationDestination.MEDALS
    NotificationType.ACHIEVEMENT_SHARED,
    NotificationType.COMMUNITY_GOAL_REACHED,
    NotificationType.COMMUNITY_EVENT_CREATED,
    NotificationType.COMMUNITY_EVENT_UPDATED -> NotificationDestination.COMMUNITY
    NotificationType.LEARNING_REMINDER -> NotificationDestination.FAVORITES
    NotificationType.WEEKLY_SUMMARY -> NotificationDestination.PROGRESS_REWARDS
    // The invitations of Quests have no route yet, and the friend has no id in the answer
    NotificationType.QUEST_INVITATION,
    NotificationType.FRIEND_REQUEST_ACCEPTED,
    NotificationType.GENERAL,
    NotificationType.UNKNOWN -> null
}
