package pe.greenminds.ecomind.main

import kotlinx.serialization.Serializable

@Serializable
data object MainMenuRoute

@Serializable
data object NotificationsRoute

@Serializable
data object SettingsRoute

// Destination of the elements of the main menu that have no screen in the design yet
@Serializable
data object PlaceholderRoute

@Serializable
data object NotificationPreferencesRoute
@Serializable
data object LanguageRoute
@Serializable
data object ThemeRoute
@Serializable
data object AccountInformationRoute
@Serializable
data object HelpRoute
