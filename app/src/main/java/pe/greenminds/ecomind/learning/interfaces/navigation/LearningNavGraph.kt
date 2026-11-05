package pe.greenminds.ecomind.learning.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.learning.interfaces.LearningDetailScreen
import pe.greenminds.ecomind.learning.interfaces.LearningScreen

// Opened from the main menu, not from the bottom bar
@Serializable
data object LearningRoute

@Serializable
data class LearningDetailRoute(val materialId: Long)

fun NavGraphBuilder.learningNavGraph(navController: NavController) {
    composable<LearningRoute> {
        LearningScreen(onOpenMaterial = { navController.navigate(LearningDetailRoute(it)) })
    }
    composable<LearningDetailRoute> { entry ->
        val route = entry.toRoute<LearningDetailRoute>()
        LearningDetailScreen(route.materialId, onBack = { navController.popBackStack() })
    }
}
