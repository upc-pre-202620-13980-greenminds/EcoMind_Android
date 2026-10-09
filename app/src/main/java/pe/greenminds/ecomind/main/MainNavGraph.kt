package pe.greenminds.ecomind.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import pe.greenminds.ecomind.quests.interfaces.categories.QuestListFilter
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestSearchRoute
import pe.greenminds.ecomind.quests.presentation.ui.QuestsScreen
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable<MainMenuRoute> {
        // The category menu is drawn over the main menu, so it is state of this screen
        var showCategories by rememberSaveable { mutableStateOf(false) }
        var selectedFilter by rememberSaveable { mutableStateOf(QuestListFilter.ENERGY) }
        var menuFilter by rememberSaveable { mutableStateOf(QuestListFilter.ENERGY) }
        val backgroundBlur by animateDpAsState(
            targetValue = if (showCategories) 8.dp else 0.dp,
            animationSpec = tween(durationMillis = if (showCategories) 420 else 220),
            label = "quest category background blur"
        )

        Box {
            QuestsScreen(
                selectedFilter = selectedFilter,
                onOpenCategories = {
                    menuFilter = selectedFilter
                    showCategories = true
                },
                onOpenQuest = { questId ->
                    navController.navigate(QuestDetailRoute(questId)) { launchSingleTop = true }
                },
                onOpenLearning = {
                    navController.navigate(LearningRoute) { launchSingleTop = true }
                },
                modifier = if (showCategories || backgroundBlur.value > 0f) {
                    // Blur needs Android 12; on older versions only the white layer is seen.
                    // The screen reader skips the main menu while the menu covers it.
                    Modifier
                        .blur(backgroundBlur)
                        .clearAndSetSemantics { }
                } else {
                    Modifier
                }
            )

            BackHandler(enabled = showCategories) { showCategories = false }

            AnimatedVisibility(
                visible = showCategories,
                enter = fadeIn(tween(durationMillis = 1)),
                exit = fadeOut(tween(durationMillis = 1, delayMillis = 560))
            ) {
                QuestCategoryMenu(
                    expanded = showCategories,
                    selectedFilter = menuFilter,
                    onSelectFilter = { filter ->
                        selectedFilter = filter
                        showCategories = false
                    },
                    onOpenSearch = {
                        showCategories = false
                        navController.navigate(QuestSearchRoute(focusSearch = true)) {
                            launchSingleTop = true
                        }
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
