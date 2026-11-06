package pe.greenminds.ecomind.quests.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.quests.interfaces.search.QuestSearchScreen
import pe.greenminds.ecomind.quests.presentation.ui.progress.ProgressScreen
import pe.greenminds.ecomind.quests.presentation.ui.progress.ProgressListScreen
import pe.greenminds.ecomind.quests.application.ProgressSection
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

// Opened from the main menu, not from the bottom bar.
// category and questType carry the name of the enum value; null means "any".
@Serializable
data class QuestSearchRoute(
    val category: String? = null,
    val questType: String? = null,
    val focusSearch: Boolean = false
)

@Serializable
data object ProgressRoute

@Serializable
data class ProgressListRoute(val section: String)

// Hook routes: screens of the design that are not built yet.
// To build one, replace PlaceholderScreen() in its composable below.

@Serializable
data class QuestDetailRoute(val questId: Long)

@Serializable
data object QuestFiltersRoute

fun NavGraphBuilder.questsNavGraph(navController: NavController) {
    composable<QuestSearchRoute> {
        QuestSearchScreen(
            onOpenQuest = { questId ->
                navController.navigate(QuestDetailRoute(questId)) { launchSingleTop = true }
            }
        )
    }

    composable<ProgressRoute> {
        ProgressScreen(
            onOpenList = { navController.navigate(ProgressListRoute(it.name)) { launchSingleTop = true } },
            onOpenQuest = { navController.navigate(QuestDetailRoute(it)) { launchSingleTop = true } }
        )
    }

    composable<ProgressListRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ProgressListRoute>()
        val section = ProgressSection.entries.firstOrNull { it.name == route.section } ?: ProgressSection.HISTORY
        ProgressListScreen(
            section = section,
            onBack = { navController.popBackStack() },
            onOpenQuest = { navController.navigate(QuestDetailRoute(it)) { launchSingleTop = true } }
        )
    }

    composable<QuestDetailRoute> {
        pe.greenminds.ecomind.quests.presentation.ui.execution.QuestExecutionScreen(onBack = { navController.popBackStack() })
    }
    composable<QuestFiltersRoute> { PlaceholderScreen() }
}
