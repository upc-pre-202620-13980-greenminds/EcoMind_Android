package pe.greenminds.ecomind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import pe.greenminds.ecomind.community.interfaces.navigation.communityNavGraph
import pe.greenminds.ecomind.gamification.interfaces.navigation.gamificationNavGraph
import pe.greenminds.ecomind.iam.interfaces.navigation.SplashRoute
import pe.greenminds.ecomind.iam.interfaces.navigation.ResetPasswordRoute
import pe.greenminds.ecomind.iam.interfaces.settings.SettingsViewModel
import pe.greenminds.ecomind.iam.interfaces.navigation.iamNavGraph
import pe.greenminds.ecomind.learning.interfaces.navigation.learningNavGraph
import pe.greenminds.ecomind.main.MainMenuRoute
import pe.greenminds.ecomind.main.mainNavGraph
import pe.greenminds.ecomind.monetization.interfaces.navigation.monetizationNavGraph
import pe.greenminds.ecomind.quests.interfaces.navigation.questsNavGraph
import pe.greenminds.ecomind.users.interfaces.navigation.usersNavGraph

// One line per bounded context: each one declares its own routes and screens
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passwordResetToken: String? = null,
    onPasswordResetHandled: () -> Unit = {},
    onAccessReset: () -> Unit = {},
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        iamNavGraph(
            navController = navController,
            onAuthenticated = {
                navController.navigate(MainMenuRoute) {
                    // Clears the access screens so going back closes the app instead of returning to them
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            },
            passwordResetToken = passwordResetToken,
            onPasswordResetHandled = onPasswordResetHandled,
            onAccessReset = onAccessReset
        )
        mainNavGraph(navController, settingsViewModel)
        usersNavGraph(navController)
        questsNavGraph(navController)
        gamificationNavGraph(navController)
        monetizationNavGraph(navController)
        communityNavGraph(navController)
        learningNavGraph(navController)
    }
    LaunchedEffect(passwordResetToken) {
        if (passwordResetToken != null &&
            navController.currentDestination?.hasRoute<ResetPasswordRoute>() != true
        ) {
            navController.navigate(ResetPasswordRoute) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
