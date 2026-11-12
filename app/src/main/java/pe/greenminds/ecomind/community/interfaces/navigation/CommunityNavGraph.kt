package pe.greenminds.ecomind.community.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.community.presentation.ui.CommunityScreen

@Serializable
data object CommunityRoute

fun NavGraphBuilder.communityNavGraph(navController: NavController) {
    composable<CommunityRoute> {
        CommunityScreen()
    }
}
