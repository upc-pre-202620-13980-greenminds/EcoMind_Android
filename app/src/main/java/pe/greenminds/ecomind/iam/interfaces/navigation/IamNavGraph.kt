package pe.greenminds.ecomind.iam.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.iam.interfaces.signin.SignInScreen
import pe.greenminds.ecomind.iam.interfaces.signup.SignUpScreen
import pe.greenminds.ecomind.iam.interfaces.splash.SplashScreen
import pe.greenminds.ecomind.iam.interfaces.recovery.RequestPasswordRecoveryScreen
import pe.greenminds.ecomind.iam.interfaces.recovery.ResetPasswordScreen
import pe.greenminds.ecomind.iam.interfaces.terms.TermsScreen

@Serializable
data object SplashRoute

@Serializable
data object SignInRoute

@Serializable
data object SignUpRoute

@Serializable
data object TermsRoute

@Serializable
data object RequestPasswordRecoveryRoute

@Serializable
data object ResetPasswordRoute

fun NavGraphBuilder.iamNavGraph(
    navController: NavController,
    onAuthenticated: () -> Unit,
    passwordResetToken: String? = null,
    onPasswordResetHandled: () -> Unit = {},
    onAccessReset: () -> Unit = {}
) {
    composable<SplashRoute> {
        if (passwordResetToken == null) SplashScreen(
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
            },
            onRecoverPassword = {
                navController.navigate(RequestPasswordRecoveryRoute)
            }
        )
    }

    composable<SignUpRoute> {
        SignUpScreen(
            // Sign up is opened from sign in, so going back returns to it
            onNavigateToSignIn = {
                navController.popBackStack()
            },
            onOpenTerms = {
                navController.navigate(TermsRoute)
            }
        )
    }

    composable<TermsRoute> {
        TermsScreen(onBack = { navController.popBackStack() })
    }

    composable<RequestPasswordRecoveryRoute> {
        RequestPasswordRecoveryScreen(onBack = {
            if (!navController.popBackStack()) {
                navController.navigate(SplashRoute) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        })
    }

    composable<ResetPasswordRoute> {
        ResetPasswordScreen(
            token = passwordResetToken.orEmpty(),
            onBack = {
                onPasswordResetHandled()
                navController.navigate(RequestPasswordRecoveryRoute) {
                    popUpTo<ResetPasswordRoute> { inclusive = true }
                }
            },
            onCompleted = {
                onPasswordResetHandled()
                onAccessReset()
            }
        )
    }
}
