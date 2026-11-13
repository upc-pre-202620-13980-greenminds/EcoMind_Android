package pe.greenminds.ecomind.users.interfaces.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen
import pe.greenminds.ecomind.learning.interfaces.FavoriteMaterialsScreen
import pe.greenminds.ecomind.learning.interfaces.navigation.LearningDetailRoute
import pe.greenminds.ecomind.users.interfaces.commitment.CommitmentScreen
import pe.greenminds.ecomind.users.interfaces.editprofile.EditProfileScreen
import pe.greenminds.ecomind.users.interfaces.findfriends.FindFriendsScreen
import pe.greenminds.ecomind.users.interfaces.findfriends.FindFriendsViewModel
import pe.greenminds.ecomind.users.interfaces.friendprofile.FriendProfileScreen
import pe.greenminds.ecomind.users.interfaces.friendrequests.FriendRequestsScreen
import pe.greenminds.ecomind.users.interfaces.invitefriends.ChooseContactsScreen
import pe.greenminds.ecomind.users.interfaces.invitefriends.InvitationCodeScreen
import pe.greenminds.ecomind.users.interfaces.invitefriends.InvitationStatusScreen
import pe.greenminds.ecomind.users.interfaces.invitefriends.InviteFriendsScreen
import pe.greenminds.ecomind.users.interfaces.profile.ProfileScreen
import pe.greenminds.ecomind.users.interfaces.profile.ProfileViewModel
import pe.greenminds.ecomind.users.interfaces.shareprofile.ShareProfileScreen

@Serializable
data object ProfileRoute

// Set by the edit profile and commitment screens so that Profile loads again when it is shown
private const val PROFILE_UPDATED_KEY = "profile_updated"

// Set by the friend requests screen so that Find friends searches again when it is shown
private const val FRIENDS_CHANGED_KEY = "friends_changed"

// Hook routes: each one is a screen of the design that is not built yet.
// To build one, replace PlaceholderScreen() in its composable below.

@Serializable
data object EditProfileRoute

@Serializable
data object ShareProfileRoute

@Serializable
data object FavoritesRoute

@Serializable
data object AddCommitmentRoute

@Serializable
data object EditCommitmentRoute

@Serializable
data object MyMedalsRoute

@Serializable
data object InviteFriendsRoute

@Serializable
data object ChooseContactsRoute

@Serializable
data object InvitationCodeRoute

@Serializable
data class InvitationStatusRoute(val friendshipId: Long)

@Serializable
data object FindFriendsRoute

@Serializable
data object FriendRequestsRoute

@Serializable
data class FriendProfileRoute(val userId: Long)

@Serializable
data object CreateFamilyRoute

@Serializable
data object ManageFamilyRoute

@Serializable
data object FamilyWeeklyReportRoute

@Serializable
data class FamilyActivityProgressRoute(val userId: Long)

fun NavGraphBuilder.usersNavGraph(navController: NavController) {
    composable<ProfileRoute> { backStackEntry ->
        val viewModel: ProfileViewModel = hiltViewModel()
        val profileUpdated by backStackEntry.savedStateHandle
            .getStateFlow(PROFILE_UPDATED_KEY, false)
            .collectAsStateWithLifecycle()

        LaunchedEffect(profileUpdated) {
            if (profileUpdated) {
                viewModel.loadProfile()
                backStackEntry.savedStateHandle[PROFILE_UPDATED_KEY] = false
            }
        }

        ProfileScreen(
            onNavigate = { route ->
                navController.navigate(route) { launchSingleTop = true }
            },
            viewModel = viewModel
        )
    }

    composable<EditProfileRoute> {
        EditProfileScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.returnToUpdatedProfile() }
        )
    }

    composable<ShareProfileRoute> {
        ShareProfileScreen(onBack = { navController.popBackStack() })
    }

    composable<InviteFriendsRoute> {
        InviteFriendsScreen(
            onBack = { navController.popBackStack() },
            onChooseContacts = {
                navController.navigate(ChooseContactsRoute) { launchSingleTop = true }
            },
            onShowCode = {
                navController.navigate(InvitationCodeRoute) { launchSingleTop = true }
            },
            onRequestSent = { friendshipId ->
                navController.markProfileForReload()
                navController.navigate(InvitationStatusRoute(friendshipId))
            }
        )
    }

    // After sending, back goes to "Invite friends" instead of the list that was just sent
    composable<ChooseContactsRoute> {
        ChooseContactsScreen(
            onBack = { navController.popBackStack() },
            onOpenStatus = { friendshipId ->
                navController.navigate(InvitationStatusRoute(friendshipId)) {
                    popUpTo<InviteFriendsRoute>()
                }
            },
            onOpenRequests = {
                navController.navigate(FriendRequestsRoute) { popUpTo<InviteFriendsRoute>() }
            },
            onFriendsChanged = { navController.markProfileForReload() }
        )
    }

    composable<InvitationCodeRoute> {
        InvitationCodeScreen(onBack = { navController.popBackStack() })
    }

    composable<InvitationStatusRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<InvitationStatusRoute>()
        InvitationStatusScreen(
            friendshipId = route.friendshipId,
            onBack = { navController.popBackStack() }
        )
    }

    composable<FavoritesRoute> {
        FavoriteMaterialsScreen(
            onBack = { navController.popBackStack() },
            onOpenMaterial = { materialId ->
                navController.navigate(LearningDetailRoute(materialId))
            }
        )
    }
    composable<AddCommitmentRoute> {
        CommitmentScreen(
            isEditing = false,
            onBack = { navController.popBackStack() },
            onSaved = { navController.returnToUpdatedProfile() }
        )
    }

    composable<EditCommitmentRoute> {
        CommitmentScreen(
            isEditing = true,
            onBack = { navController.popBackStack() },
            onSaved = { navController.returnToUpdatedProfile() }
        )
    }

    composable<MyMedalsRoute> {
        pe.greenminds.ecomind.gamification.interfaces.achievements.AchievementsScreen(
            onBack = { navController.popBackStack() },
            onOpenAchievement = { navController.navigate(pe.greenminds.ecomind.gamification.interfaces.navigation.AchievementDetailRoute(it)) },
            onSignIn = { navController.navigate(pe.greenminds.ecomind.iam.interfaces.navigation.SignInRoute) }
        )
    }
    composable<FindFriendsRoute> { backStackEntry ->
        val viewModel: FindFriendsViewModel = hiltViewModel()
        val requestsAnswered by backStackEntry.savedStateHandle
            .getStateFlow(FRIENDS_CHANGED_KEY, false)
            .collectAsStateWithLifecycle()

        // A request answered after "Reply" changes the action of that row
        LaunchedEffect(requestsAnswered) {
            if (requestsAnswered) {
                viewModel.search()
                backStackEntry.savedStateHandle[FRIENDS_CHANGED_KEY] = false
            }
        }

        FindFriendsScreen(
            onBack = { navController.popBackStack() },
            onReply = {
                navController.navigate(FriendRequestsRoute) { launchSingleTop = true }
            },
            onFriendsChanged = { navController.markProfileForReload() },
            viewModel = viewModel
        )
    }

    composable<FriendRequestsRoute> {
        FriendRequestsScreen(
            onBack = { navController.popBackStack() },
            onOpenInvitation = { friendshipId ->
                navController.navigate(InvitationStatusRoute(friendshipId))
            },
            onFriendsChanged = {
                navController.markProfileForReload()
                navController.previousBackStackEntry?.savedStateHandle?.set(FRIENDS_CHANGED_KEY, true)
            }
        )
    }

    composable<FriendProfileRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<FriendProfileRoute>()
        FriendProfileScreen(
            friendId = route.userId,
            onBack = { navController.popBackStack() },
            // Back to the Friends tab, which loads the list again without the removed friend
            onRemoved = {
                navController.markProfileForReload()
                navController.popBackStack()
            }
        )
    }
    composable<CreateFamilyRoute> { PlaceholderScreen() }
    composable<ManageFamilyRoute> { PlaceholderScreen() }
    composable<FamilyWeeklyReportRoute> { PlaceholderScreen() }
    composable<FamilyActivityProgressRoute> { PlaceholderScreen() }
}

// Goes back to Profile and tells it to load the profile again
private fun NavController.returnToUpdatedProfile() {
    previousBackStackEntry?.savedStateHandle?.set(PROFILE_UPDATED_KEY, true)
    popBackStack()
}

// Profile loads again when it is shown, even if the change was made two screens away
private fun NavController.markProfileForReload() {
    val profileEntry = runCatching { getBackStackEntry<ProfileRoute>() }.getOrNull()
    profileEntry?.savedStateHandle?.set(PROFILE_UPDATED_KEY, true)
}
