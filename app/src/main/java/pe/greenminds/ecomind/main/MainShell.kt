package pe.greenminds.ecomind.main

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.greenminds.ecomind.learning.interfaces.navigation.LearningRoute
import pe.greenminds.ecomind.navigation.AppNavHost
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestListRoute
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar

// Screens that hang from the main menu: they keep the bars and the main menu item active.
// A new screen of that kind is added to this list.
private val mainMenuChildRoutes = listOf(QuestListRoute::class, LearningRoute::class)

// Frame of the app: the top bar and the bottom bar live here, each section only draws its content
@Composable
fun MainShell(viewModel: MainShellViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedItem = selectedItemFor(backStackEntry?.destination)
    val showBars = selectedItem != null

    LaunchedEffect(showBars) {
        if (showBars) {
            viewModel.loadBalances()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showBars) {
                EcoMindTopBar(
                    gemBalance = state.gemBalance,
                    ecopoints = state.ecopoints,
                    onNotificationsClick = {
                        navController.navigate(NotificationsRoute) { launchSingleTop = true }
                    },
                    onSettingsClick = {
                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        bottomBar = {
            if (selectedItem != null) {
                MainNavigationBar(
                    selectedItem = selectedItem,
                    onItemClick = { item -> navigateToSection(navController, item) },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            // The space taken by the bars is marked as consumed so the screens do not add it again
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

// Null means the destination is shown without bars (loading, access and placeholder screens)
private fun selectedItemFor(destination: NavDestination?): NavigationItem? {
    if (destination == null) return null

    val section = NavigationItem.entries.firstOrNull { item ->
        destination.hasRoute(item.route::class)
    }
    if (section != null) return section

    val hangsFromMainMenu = mainMenuChildRoutes.any { route -> destination.hasRoute(route) }
    return if (hangsFromMainMenu) NavigationItem.MAIN_MENU else null
}

private fun navigateToSection(navController: NavHostController, item: NavigationItem) {
    navController.navigate(item.route) {
        // Main menu stays at the bottom of the stack, so going back from a section returns to it
        popUpTo<MainMenuRoute> {
            // Keeps what the section being left had on screen
            saveState = true
        }
        // Tapping the active item does not open the section twice
        launchSingleTop = true
        // Brings back what the section had when it was left
        restoreState = true
    }
}
