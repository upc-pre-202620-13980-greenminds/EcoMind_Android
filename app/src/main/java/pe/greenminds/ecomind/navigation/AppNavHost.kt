package pe.greenminds.ecomind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.iam.interfaces.navigation.SplashRoute
import pe.greenminds.ecomind.iam.interfaces.navigation.iamNavGraph
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object MainMenuRoute

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
            PlaceholderScreen()
        }
    }
}
