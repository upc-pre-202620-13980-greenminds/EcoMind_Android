package pe.greenminds.ecomind.main

import pe.greenminds.ecomind.users.interfaces.navigation.AddCommitmentRoute
import pe.greenminds.ecomind.users.interfaces.navigation.ChooseContactsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.InvitationCodeRoute
import pe.greenminds.ecomind.users.interfaces.navigation.InvitationStatusRoute
import pe.greenminds.ecomind.users.interfaces.navigation.InviteFriendsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.EditCommitmentRoute
import pe.greenminds.ecomind.users.interfaces.navigation.EditProfileRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FindFriendsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FriendProfileRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FriendRequestsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FavoritesRoute
import pe.greenminds.ecomind.users.interfaces.navigation.ShareProfileRoute
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute
import pe.greenminds.ecomind.shared.infrastructure.offline.isOnline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.greenminds.ecomind.learning.interfaces.navigation.LearningRoute
import pe.greenminds.ecomind.learning.interfaces.TutorialOverlay
import pe.greenminds.ecomind.navigation.AppNavHost
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestSearchRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.FamilyPlanBuilderRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.FamilyPlanListRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.FamilyPlanDetailRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.ProgressRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.FamilyQuestStartedRoute
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar
import pe.greenminds.ecomind.iam.interfaces.settings.SettingsViewModel

// Screens that hang from the main menu: they keep the bars and the main menu item active.
// A new screen of that kind is added to this list.
private val mainMenuChildRoutes = listOf(QuestSearchRoute::class, LearningRoute::class, pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute::class)

// Frame of the app: the top bar and the bottom bar live here, each section only draws its content
@Composable
fun MainShell(
    passwordResetToken: String? = null,
    onPasswordResetHandled: () -> Unit = {},
    viewModel: MainShellViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var navigationVersion by rememberSaveable { mutableIntStateOf(0) }
    key(navigationVersion) {
        MainShellContent(
            viewModel = viewModel,
            settingsViewModel = settingsViewModel,
            passwordResetToken = passwordResetToken,
            onPasswordResetHandled = onPasswordResetHandled,
            onAccessReset = {
                viewModel.clearHeader()
                navigationVersion++
            }
        )
    }
}

@Composable
private fun MainShellContent(
    viewModel: MainShellViewModel,
    settingsViewModel: SettingsViewModel,
    passwordResetToken: String?,
    onPasswordResetHandled: () -> Unit,
    onAccessReset: () -> Unit
) {
    val navController = rememberNavController()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val settingsState = settingsViewModel.state.collectAsStateWithLifecycle().value
    val session = viewModel.session.collectAsStateWithLifecycle().value
    val online = isOnline()
    var sessionExpired by androidx.compose.runtime.remember(session) {
        androidx.compose.runtime.mutableStateOf(session?.isExpired(System.currentTimeMillis()) == true)
    }
    LaunchedEffect(session) {
        val expiresAt = session?.expiresAtMillis ?: return@LaunchedEffect
        kotlinx.coroutines.delay((expiresAt - System.currentTimeMillis()).coerceAtLeast(0))
        sessionExpired = true
    }

    val resetAccess = {
        NavigationItem.entries.forEach { navController.clearBackStack(it.route) }
        navController.popBackStack(navController.graph.id, inclusive = true)
        onAccessReset()
    }

    LaunchedEffect(settingsState.isSignedOut) {
        if (settingsState.isSignedOut) {
            settingsViewModel.acknowledgeSignOut()
            resetAccess()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val detailRoute = backStackEntry?.takeIf {
        it.destination.hasRoute<pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute>()
    }?.toRoute<pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute>()
    val selectedItem = if (detailRoute?.previewOnly == true || detailRoute?.familyPlanId != null) NavigationItem.PROGRESS
        else selectedItemFor(backStackEntry?.destination)
    val showBars = selectedItem != null
    val showTopBar = showBars && backStackEntry?.destination?.hasRoute<pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute>() != true &&
        backStackEntry?.destination?.hasRoute<FamilyQuestStartedRoute>() != true

    LaunchedEffect(showTopBar, backStackEntry?.id, online) {
        if (showTopBar) {
            viewModel.loadHeaderCounters()
            if (online && !sessionExpired) viewModel.loadUnreadNotifications()
        }
    }

    androidx.compose.foundation.layout.Box {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                EcoMindTopBar(
                    gemBalance = state.gemBalance,
                    streak = state.streak,
                    onNotificationsClick = {
                        navController.navigate(NotificationsRoute) { launchSingleTop = true }
                    },
                    onSettingsClick = {
                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                    },
                    modifier = Modifier.statusBarsPadding(),
                    unreadNotifications = state.unreadNotifications
                )
                if (sessionExpired && online) {
                    TextButton(onClick = {
                        viewModel.clearHeader()
                        navController.navigate(SignInRoute) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }) { Text(stringResource(R.string.offline_sign_in_again)) }
                } else if (sessionExpired || !online) {
                    Text(
                        stringResource(if (sessionExpired) R.string.offline_session_expired else R.string.offline_saved_data),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                }
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
            passwordResetToken = passwordResetToken,
            onPasswordResetHandled = onPasswordResetHandled,
            onAccessReset = resetAccess,
            settingsViewModel = settingsViewModel,
            // The space taken by the bars is marked as consumed so the screens do not add it again
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
    TutorialOverlay(active = showBars)
    }
}

// Null means the destination is shown without bars (loading, access and placeholder screens)
private fun selectedItemFor(destination: NavDestination?): NavigationItem? {
    if (destination == null) return null
    if (destination.hasRoute<pe.greenminds.ecomind.users.interfaces.navigation.MyMedalsRoute>() ||
        destination.hasRoute<pe.greenminds.ecomind.gamification.interfaces.navigation.AchievementDetailRoute>() ||
        destination.hasRoute<pe.greenminds.ecomind.gamification.interfaces.navigation.ShareAchievementRoute>()) return NavigationItem.PROFILE
    if (destination.hasRoute<pe.greenminds.ecomind.gamification.interfaces.navigation.GamificationProgressRoute>() ||
        destination.hasRoute<pe.greenminds.ecomind.gamification.interfaces.navigation.RewardHistoryRoute>() ||
        destination.hasRoute<pe.greenminds.ecomind.gamification.interfaces.navigation.GroupAchievementsRoute>()) return NavigationItem.RANKING
    if (destination.hasRoute<FamilyPlanBuilderRoute>() || destination.hasRoute<FamilyPlanListRoute>() ||
        destination.hasRoute<FamilyPlanDetailRoute>() ||
        destination.hasRoute<FamilyQuestStartedRoute>()) return NavigationItem.PROGRESS
    if (destination.hasRoute<pe.greenminds.ecomind.quests.interfaces.navigation.ProgressListRoute>()) return NavigationItem.PROGRESS
    if (destination.hasRoute<SettingsRoute>() || destination.hasRoute<HelpSupportRoute>() ||
        destination.hasRoute<NotificationsRoute>() ||
        destination.hasRoute<NotificationPreferencesRoute>() ||
        destination.hasRoute<LanguageRoute>() || destination.hasRoute<ThemeRoute>() ||
        destination.hasRoute<FaqRoute>() || destination.hasRoute<FaqDetailRoute>()) return NavigationItem.PROFILE
    // Sub screens of Profile keep the bars with the Profile item active
    if (destination.hasRoute<EditProfileRoute>() || destination.hasRoute<ShareProfileRoute>() ||
        destination.hasRoute<FavoritesRoute>() ||
        destination.hasRoute<AddCommitmentRoute>() || destination.hasRoute<EditCommitmentRoute>() ||
        destination.hasRoute<FindFriendsRoute>() || destination.hasRoute<FriendRequestsRoute>() ||
        destination.hasRoute<InviteFriendsRoute>() || destination.hasRoute<ChooseContactsRoute>() ||
        destination.hasRoute<InvitationCodeRoute>() || destination.hasRoute<InvitationStatusRoute>() ||
        destination.hasRoute<FriendProfileRoute>()) {
        return NavigationItem.PROFILE
    }

    val section = NavigationItem.entries.firstOrNull { item ->
        destination.hasRoute(item.route::class)
    }
    if (section != null) return section

    val hangsFromMainMenu = mainMenuChildRoutes.any { route -> destination.hasRoute(route) }
    return if (hangsFromMainMenu) NavigationItem.MAIN_MENU else null
}

private fun navigateToSection(navController: NavHostController, item: NavigationItem) {
    if (item == NavigationItem.PROGRESS) {
        if (navController.currentDestination?.hasRoute<ProgressRoute>() == true) return
        if (navController.popBackStack<ProgressRoute>(inclusive = false)) return
        navController.navigate(ProgressRoute) {
            popUpTo<MainMenuRoute> { saveState = true }
            launchSingleTop = true
            restoreState = false
        }
        return
    }
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
