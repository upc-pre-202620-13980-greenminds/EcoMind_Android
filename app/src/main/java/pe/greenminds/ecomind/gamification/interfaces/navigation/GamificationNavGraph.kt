package pe.greenminds.ecomind.gamification.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.gamification.interfaces.ranking.RankingScreen
import pe.greenminds.ecomind.gamification.interfaces.achievements.AchievementDetailScreen
import pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute

@Serializable
data object RankingRoute

@Serializable
data class AchievementDetailRoute(val achievementId: String)

// Ranking and achievement detail belong to Gamification; Profile owns the collection entry point.
fun NavGraphBuilder.gamificationNavGraph(navController: NavController) {
    composable<RankingRoute> {
        RankingScreen()
    }
    composable<AchievementDetailRoute> { entry ->
        AchievementDetailScreen(
            achievementId = entry.toRoute<AchievementDetailRoute>().achievementId,
            onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } }
        )
    }
}
