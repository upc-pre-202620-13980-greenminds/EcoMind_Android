package pe.greenminds.ecomind.settings.domain.model

enum class AppLanguage(val tag: String) { ENGLISH("en"), SPANISH("es-419") }
enum class AppTheme { LIGHT, DARK }
enum class NotificationCategory { QUESTS, LEARNING, ACHIEVEMENTS, WEEKLY_SUMMARY, EVENTS, COMMUNITY }
data class AppPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val theme: AppTheme = AppTheme.LIGHT,
    val enabledNotifications: Set<NotificationCategory> = NotificationCategory.entries.toSet() - NotificationCategory.WEEKLY_SUMMARY
)
