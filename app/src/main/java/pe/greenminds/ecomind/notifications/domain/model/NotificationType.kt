package pe.greenminds.ecomind.notifications.domain.model

// What produced the notification, the same values of the web services. The app uses it to know
// where to go when the user taps it; a value this version does not know yet is UNKNOWN.
enum class NotificationType {
    GENERAL,
    FRIEND_REQUEST_ACCEPTED,
    QUEST_INVITATION,
    ACHIEVEMENT_UNLOCKED,
    ACHIEVEMENT_SHARED,
    COMMUNITY_GOAL_REACHED,
    COMMUNITY_EVENT_CREATED,
    COMMUNITY_EVENT_UPDATED,
    LEARNING_REMINDER,
    WEEKLY_SUMMARY,
    UNKNOWN
}
