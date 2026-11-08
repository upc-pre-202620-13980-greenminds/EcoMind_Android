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
import pe.greenminds.ecomind.quests.presentation.ui.family.FamilyPlanBuilderScreen
import pe.greenminds.ecomind.quests.presentation.ui.family.FamilyPlanListScreen
import pe.greenminds.ecomind.quests.presentation.ui.family.FamilyPlanDetailScreen
import pe.greenminds.ecomind.quests.presentation.ui.family.FamilyQuestStartedScreen
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus

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
data class QuestDetailRoute(
    val questId: Long,
    val previewOnly: Boolean = false,
    val familyPlanId: Long? = null,
    val familyPlanItemId: Long? = null
)

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
            onOpenQuest = { navController.navigate(QuestDetailRoute(it)) { launchSingleTop = true } },
            onCreateFamilyPlan = { navController.navigate(FamilyPlanBuilderRoute()) { launchSingleTop = true } },
            onLoadFamilyPlans = { navController.navigate(FamilyPlanListRoute) { launchSingleTop = true } },
            onOpenFamilyPlan = { navController.navigate(FamilyPlanDetailRoute(it)) { launchSingleTop = true } }
        )
    }

    composable<ProgressListRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ProgressListRoute>()
        val section = ProgressSection.entries.firstOrNull { it.name == route.section } ?: ProgressSection.HISTORY
        ProgressListScreen(
            section = section,
            onBack = { navController.popBackStack() },
            onOpenQuest = { navController.navigate(QuestDetailRoute(it)) { launchSingleTop = true } },
            onOpenFamilyPlan = { id ->
                if (id != null) navController.navigate(FamilyPlanDetailRoute(id)) { launchSingleTop = true }
                else navController.navigate(FamilyPlanListRoute) { launchSingleTop = true }
            }
        )
    }

    composable<QuestDetailRoute> { entry ->
        val route = entry.toRoute<QuestDetailRoute>()
        pe.greenminds.ecomind.quests.presentation.ui.execution.QuestExecutionScreen(
            onBack = { navController.popBackStack() }, previewOnly = route.previewOnly)
    }
    composable<QuestFiltersRoute> { PlaceholderScreen() }

    composable<FamilyPlanBuilderRoute> { entry ->
        val route = entry.toRoute<FamilyPlanBuilderRoute>()
        FamilyPlanBuilderScreen(route.planId, onBack = { navController.popBackStack() },
            onLoad = { navController.navigate(FamilyPlanListRoute) { launchSingleTop = true } },
            onViewQuest = { navController.navigate(QuestDetailRoute(it, previewOnly = true)) { launchSingleTop = true } },
            onActivated = { id -> navController.navigate(FamilyQuestStartedRoute(id)) {
                popUpTo<FamilyPlanBuilderRoute> { inclusive = true }
                launchSingleTop = true
            } })
    }
    composable<FamilyPlanListRoute> {
        FamilyPlanListScreen(onBack = { navController.popBackStack() }, onLoad = { plan, canEdit ->
            if (plan.status == FamilyPlanStatus.DRAFT && canEdit) navController.navigate(FamilyPlanBuilderRoute(plan.id)) {
                popUpTo<FamilyPlanBuilderRoute> { inclusive = true }
                launchSingleTop = true
            } else navController.navigate(FamilyPlanDetailRoute(plan.id)) { launchSingleTop = true }
        })
    }
    composable<FamilyPlanDetailRoute> { entry ->
        val route = entry.toRoute<FamilyPlanDetailRoute>()
        FamilyPlanDetailScreen(route.planId, onBack = { navController.popBackStack() },
            onOpenActivities = { questId, itemId ->
                navController.navigate(QuestDetailRoute(questId, familyPlanId = route.planId, familyPlanItemId = itemId)) {
                    launchSingleTop = true
                }
            },
            onEdit = { navController.navigate(FamilyPlanBuilderRoute(route.planId)) { launchSingleTop = true } })
    }
    composable<FamilyQuestStartedRoute> { entry ->
        val route = entry.toRoute<FamilyQuestStartedRoute>()
        FamilyQuestStartedScreen(onFinished = {
            navController.navigate(FamilyPlanDetailRoute(route.planId)) {
                popUpTo<FamilyQuestStartedRoute> { inclusive = true }
                launchSingleTop = true
            }
        })
    }
}
