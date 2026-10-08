package pe.greenminds.ecomind.iam.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.iam.interfaces.signin.SignInScreen
import pe.greenminds.ecomind.iam.interfaces.splash.SplashScreen
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object SplashRoute

@Serializable
data object SignInRoute

@Serializable
data object SignUpRoute

fun NavGraphBuilder.iamNavGraph(
    navController: NavController,
    onAuthenticated: () -> Unit
) {
    composable<SplashRoute> {
        SplashScreen(
            onNavigateToSignIn = {
                navController.navigate(SignInRoute) {
                    // The loading screen leaves the back stack so "back" does not return to it
                    popUpTo<SplashRoute> {
                        inclusive = true
                    }
                }
            },
            onNavigateToMainMenu = onAuthenticated
        )
    }

    composable<SignInRoute> {
        SignInScreen(
            onSignedIn = onAuthenticated,
            onNavigateToSignUp = {
                navController.navigate(SignUpRoute)
            }
        )
    }

    composable<SignUpRoute> {
        PlaceholderScreen()
    }
}
