package pe.greenminds.ecomind.users.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object ProfileRoute

fun NavGraphBuilder.usersNavGraph(navController: NavController) {
    composable<ProfileRoute> {
        PlaceholderScreen()
    }
}
