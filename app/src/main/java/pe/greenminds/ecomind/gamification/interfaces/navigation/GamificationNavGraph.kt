package pe.greenminds.ecomind.gamification.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.gamification.interfaces.ranking.RankingScreen

@Serializable
data object RankingRoute

// The design has no sub screens for the ranking yet, so navController is not used here.
// It stays as a parameter because every context exposes its graph with the same shape.
fun NavGraphBuilder.gamificationNavGraph(navController: NavController) {
    composable<RankingRoute> {
        RankingScreen()
    }
}
