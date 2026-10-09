package pe.greenminds.ecomind.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute
import pe.greenminds.ecomind.quests.presentation.ui.QuestsScreen
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable<MainMenuRoute> {
        // The category menu is drawn over the main menu, so it is state of this screen
        var showCategories by rememberSaveable { mutableStateOf(false) }

        Box {
            QuestsScreen(
                onOpenCategories = { showCategories = true },
                onOpenQuest = { questId ->
                    navController.navigate(QuestDetailRoute(questId)) { launchSingleTop = true }
                },
                onOpenLearning = {
                    navController.navigate(LearningRoute) { launchSingleTop = true }
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
                    onOpenSearch = { route ->
                        showCategories = false
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onDismiss = { showCategories = false }
                )
            }
        }
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
