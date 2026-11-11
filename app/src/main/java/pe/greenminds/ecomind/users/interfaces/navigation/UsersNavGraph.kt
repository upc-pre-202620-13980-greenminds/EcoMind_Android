package pe.greenminds.ecomind.users.interfaces.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen
import pe.greenminds.ecomind.users.interfaces.editprofile.EditProfileScreen
import pe.greenminds.ecomind.users.interfaces.profile.ProfileScreen
import pe.greenminds.ecomind.users.interfaces.profile.ProfileViewModel
import pe.greenminds.ecomind.users.interfaces.shareprofile.ShareProfileScreen

@Serializable
data object ProfileRoute

// Set by the edit profile screen so that Profile loads the new name when it is shown again
private const val PROFILE_UPDATED_KEY = "profile_updated"

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
            onSaved = {
                navController.previousBackStackEntry?.savedStateHandle?.set(PROFILE_UPDATED_KEY, true)
                navController.popBackStack()
            }
        )
    }

    composable<ShareProfileRoute> {
        ShareProfileScreen(onBack = { navController.popBackStack() })
    }

    composable<FavoritesRoute> { PlaceholderScreen() }
    composable<AddCommitmentRoute> { PlaceholderScreen() }
    composable<EditCommitmentRoute> { PlaceholderScreen() }
    composable<MyMedalsRoute> { PlaceholderScreen() }
    composable<InviteFriendsRoute> { PlaceholderScreen() }
    composable<FindFriendsRoute> { PlaceholderScreen() }
    composable<FriendRequestsRoute> { PlaceholderScreen() }
    composable<FriendProfileRoute> { PlaceholderScreen() }
    composable<CreateFamilyRoute> { PlaceholderScreen() }
    composable<ManageFamilyRoute> { PlaceholderScreen() }
    composable<FamilyWeeklyReportRoute> { PlaceholderScreen() }
    composable<FamilyActivityProgressRoute> { PlaceholderScreen() }
}
