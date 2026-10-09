package pe.greenminds.ecomind.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import pe.greenminds.ecomind.notifications.interfaces.inbox.NotificationsScreen
import pe.greenminds.ecomind.settings.interfaces.preferences.*
import pe.greenminds.ecomind.users.interfaces.navigation.MyMedalsRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestListRoute
import pe.greenminds.ecomind.community.interfaces.navigation.CommunityRoute
import pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import pe.greenminds.ecomind.learning.interfaces.navigation.LearningRoute
import pe.greenminds.ecomind.quests.interfaces.categories.QuestCategoryMenu
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable<MainMenuRoute> {
        // The category menu is drawn over the main menu, so it is state of this screen
        var showCategories by rememberSaveable { mutableStateOf(false) }

        Box {
            MainMenuScreen(
                onOpenQuests = { showCategories = true },
                onOpenLearning = {
                    navController.navigate(LearningRoute) { launchSingleTop = true }
                },
                onOpenPlaceholder = {
                    navController.navigate(PlaceholderRoute) { launchSingleTop = true }
                },
                modifier = if (showCategories) {
                    // Blur needs Android 12; on older versions only the white layer is seen.
                    // The screen reader skips the main menu while the menu covers it.
                    Modifier
                        .blur(8.dp)
                        .clearAndSetSemantics { }
                } else {
                    Modifier
                }
            )

            if (showCategories) {
                BackHandler { showCategories = false }

                QuestCategoryMenu(
                    onOpenList = { route ->
                        showCategories = false
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onDismiss = { showCategories = false }
                )
            }
        }
    }

    composable<NotificationsRoute> {
        NotificationsScreen(
            onBack = { navController.popBackStack() },
            onOpen = { id ->
                val route: Any = when (id) {
                    "medal" -> MyMedalsRoute
                    "learning" -> LearningRoute
                    "quest" -> QuestListRoute()
                    else -> CommunityRoute
                }
                navController.navigate(route) { launchSingleTop = true }
            },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } }
        )
    }
    composable<SettingsRoute> {
        SettingsScreen(
            onBack = { navController.popBackStack() },
            onOpen = { page ->
                val route: Any = when (page) {
                    "notifications" -> NotificationPreferencesRoute
                    "language" -> LanguageRoute
                    "theme" -> ThemeRoute
                    "account" -> AccountInformationRoute
                    else -> HelpRoute
                }
                navController.navigate(route) { launchSingleTop = true }
            },
            onSignedOut = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } }
        )
    }
    composable<NotificationPreferencesRoute> { NotificationPreferencesScreen(onBack = { navController.popBackStack() }) }
    composable<LanguageRoute> { AppearanceScreen(language = true, onBack = { navController.popBackStack() }) }
    composable<ThemeRoute> { AppearanceScreen(language = false, onBack = { navController.popBackStack() }) }
    composable<AccountInformationRoute> { AccountScreen(onBack = { navController.popBackStack() }) }
    composable<HelpRoute> { HelpScreen(onBack = { navController.popBackStack() }) }

    composable<PlaceholderRoute> {
        PlaceholderScreen()
    }
}
