package pe.greenminds.ecomind.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import pe.greenminds.ecomind.learning.interfaces.navigation.LearningRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestListRoute
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable<MainMenuRoute> {
        MainMenuScreen(
            onOpenQuests = {
                navController.navigate(QuestListRoute) { launchSingleTop = true }
            },
            onOpenLearning = {
                navController.navigate(LearningRoute) { launchSingleTop = true }
            },
            onOpenPlaceholder = {
                navController.navigate(PlaceholderRoute) { launchSingleTop = true }
            }
        )
    }

    composable<NotificationsRoute> {
        PlaceholderScreen()
    }

    composable<SettingsRoute> {
        PlaceholderScreen()
    }

    composable<PlaceholderRoute> {
        PlaceholderScreen()
    }
}
