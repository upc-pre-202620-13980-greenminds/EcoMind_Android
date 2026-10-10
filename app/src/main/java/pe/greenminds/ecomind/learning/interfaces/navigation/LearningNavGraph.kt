package pe.greenminds.ecomind.learning.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

// Opened from the main menu, not from the bottom bar
@Serializable
data object LearningRoute

fun NavGraphBuilder.learningNavGraph(navController: NavController) {
    composable<LearningRoute> {
        PlaceholderScreen()
    }
}
