package pe.greenminds.ecomind.shared.domain.model

// Languages of frame 73; the tag is the one of the resources (values-b+es+419)
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    SPANISH("es-419")
}

// Frame 74 only offers light and dark
enum class AppTheme {
    LIGHT,
    DARK
}

// Categories of frame 70 (TS-008), in the order of the design
enum class NotificationCategory {
    NEW_AND_DAILY_QUESTS,
    LEARNING_REMINDERS,
    ACHIEVEMENTS_AND_BADGES,
    WEEKLY_PROGRESS_SUMMARY,
    COMMUNITY_EVENTS,
    COMMUNITY_ACTIVITY
}

data class AppPreferences(
    val language: AppLanguage,
    val theme: AppTheme,
    // A category that was never changed is enabled
    val enabledCategories: Set<NotificationCategory>
)
