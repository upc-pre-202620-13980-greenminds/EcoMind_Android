package pe.greenminds.ecomind.monetization.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

@Serializable
data object StoreRoute

fun NavGraphBuilder.monetizationNavGraph(navController: NavController) {
    composable<StoreRoute> {
        PlaceholderScreen()
    }
}
