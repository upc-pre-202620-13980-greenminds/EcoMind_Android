package pe.greenminds.ecomind.quests.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import pe.greenminds.ecomind.quests.interfaces.detail.QuestDetailScreen
import pe.greenminds.ecomind.gamification.interfaces.navigation.GamificationProgressRoute
import pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.quests.interfaces.list.QuestListScreen
import pe.greenminds.ecomind.quests.interfaces.progress.ProgressScreen
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

// Opened from the main menu, not from the bottom bar.
// category and questType carry the name of the enum value; null means "any".
@Serializable
data class QuestListRoute(
    val category: String? = null,
    val questType: String? = null,
    val focusSearch: Boolean = false
)

@Serializable
data object ProgressRoute

// Hook routes: screens of the design that are not built yet.
// To build one, replace PlaceholderScreen() in its composable below.

@Serializable
data class QuestDetailRoute(val questId: Long)

@Serializable
data object QuestFiltersRoute

fun NavGraphBuilder.questsNavGraph(navController: NavController) {
    composable<QuestListRoute> {
        QuestListScreen(
            onOpenQuest = { questId ->
                navController.navigate(QuestDetailRoute(questId)) { launchSingleTop = true }
            },
            onOpenFilters = {
                navController.navigate(QuestFiltersRoute) { launchSingleTop = true }
            }
        )
    }

    composable<ProgressRoute> {
        ProgressScreen()
    }

    composable<QuestDetailRoute> { entry ->
        QuestDetailScreen(entry.toRoute<QuestDetailRoute>().questId, onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } },
            onProgress = { navController.navigate(GamificationProgressRoute) })
    }
    composable<QuestFiltersRoute> { PlaceholderScreen() }
}
