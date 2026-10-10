package pe.greenminds.ecomind.gamification.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.gamification.interfaces.ranking.RankingScreen
import pe.greenminds.ecomind.gamification.interfaces.achievements.AchievementDetailScreen
import pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute
import pe.greenminds.ecomind.gamification.interfaces.progress.*
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import pe.greenminds.ecomind.users.interfaces.navigation.MyMedalsRoute

@Serializable
data object RankingRoute

@Serializable
data class AchievementDetailRoute(val achievementId: String)

@Serializable data object GamificationProgressRoute
@Serializable data object RewardHistoryRoute
@Serializable data class GroupAchievementsRoute(val groupId: Long, val name: String, val scope: String)
@Serializable data class ShareAchievementRoute(val awardId: String)

// Ranking and achievement detail belong to Gamification; Profile owns the collection entry point.
fun NavGraphBuilder.gamificationNavGraph(navController: NavController) {
    composable<RankingRoute> {
        RankingScreen(onProgress = { navController.navigate(GamificationProgressRoute) })
    }
    composable<GamificationProgressRoute> {
        ProgressScreen(onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } },
            onHistory = { navController.navigate(RewardHistoryRoute) },
            onMedals = { navController.navigate(MyMedalsRoute) },
            onGroup = { navController.navigate(GroupAchievementsRoute(it.id, it.name, it.scope)) })
    }
    composable<RewardHistoryRoute> {
        HistoryScreen(onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } })
    }
    composable<GroupAchievementsRoute> { entry ->
        val route = entry.toRoute<GroupAchievementsRoute>()
        GroupAchievementsScreen(AchievementGroup(route.groupId, route.name, route.scope), onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } })
    }
    composable<ShareAchievementRoute> { entry ->
        ShareAchievementScreen(entry.toRoute<ShareAchievementRoute>().awardId, onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } })
    }
    composable<AchievementDetailRoute> { entry ->
        AchievementDetailScreen(
            achievementId = entry.toRoute<AchievementDetailRoute>().achievementId,
            onBack = { navController.popBackStack() },
            onShare = { navController.navigate(ShareAchievementRoute(it)) },
            onSignIn = { navController.navigate(SignInRoute) { popUpTo(navController.graph.id) { inclusive = true } } }
        )
    }
}
