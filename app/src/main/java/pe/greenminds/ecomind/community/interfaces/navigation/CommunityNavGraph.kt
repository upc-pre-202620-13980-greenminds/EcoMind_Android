package pe.greenminds.ecomind.community.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.community.interfaces.events.CommunityScreen

@Serializable
data object CommunityRoute

// Only the main screen of the community is built, so navController is not used here yet.
// It stays as a parameter because every context exposes its graph with the same shape.
fun NavGraphBuilder.communityNavGraph(navController: NavController) {
    composable<CommunityRoute> {
        CommunityScreen()
    }
}
