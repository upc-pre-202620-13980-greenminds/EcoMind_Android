package pe.greenminds.ecomind.monetization.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.monetization.interfaces.store.StoreScreen

@Serializable
data object StoreRoute

// Only the main screen of the store is built, so navController is not used here yet.
// It stays as a parameter because every context exposes its graph with the same shape.
fun NavGraphBuilder.monetizationNavGraph(navController: NavController) {
    composable<StoreRoute> {
        StoreScreen()
    }
}
