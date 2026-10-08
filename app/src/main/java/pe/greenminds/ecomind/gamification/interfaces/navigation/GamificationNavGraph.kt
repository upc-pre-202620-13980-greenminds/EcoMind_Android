package pe.greenminds.ecomind.gamification.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object RankingRoute

fun NavGraphBuilder.gamificationNavGraph(navController: NavController) {
    composable<RankingRoute> {
        PlaceholderScreen()
    }
}
