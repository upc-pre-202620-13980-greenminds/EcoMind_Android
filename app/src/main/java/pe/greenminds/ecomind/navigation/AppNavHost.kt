package pe.greenminds.ecomind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.iam.interfaces.navigation.SplashRoute
import pe.greenminds.ecomind.iam.interfaces.navigation.iamNavGraph
import pe.greenminds.ecomind.main.MainMenuScreen
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object MainMenuRoute

// Single destination for every section that is not built yet
@Serializable
data object PlaceholderRoute

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        iamNavGraph(
            navController = navController,
            onAuthenticated = {
                navController.navigate(MainMenuRoute) {
                    // Clears the access screens so "back" closes the app instead of returning to them
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        )

        composable<MainMenuRoute> {
            MainMenuScreen(
                onNavigateToPlaceholder = {
                    navController.navigate(PlaceholderRoute) {
                        // Two quick taps open the placeholder only once
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<PlaceholderRoute> {
            PlaceholderScreen()
        }
    }
}
