package pe.greenminds.ecomind.quests.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen

// Opened from the main menu, not from the bottom bar
@Serializable
data object QuestListRoute

@Serializable
data object ProgressRoute

fun NavGraphBuilder.questsNavGraph(navController: NavController) {
    composable<QuestListRoute> {
        PlaceholderScreen()
    }

    composable<ProgressRoute> {
        PlaceholderScreen()
    }
}
