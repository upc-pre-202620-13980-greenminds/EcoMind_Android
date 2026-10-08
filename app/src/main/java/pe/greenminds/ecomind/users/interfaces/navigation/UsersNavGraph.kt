package pe.greenminds.ecomind.users.interfaces.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.greenminds.ecomind.shared.interfaces.components.PlaceholderScreen
import pe.greenminds.ecomind.users.interfaces.profile.ProfileScreen

@Serializable
data object ProfileRoute

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
    composable<ProfileRoute> {
        ProfileScreen(
            onNavigate = { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        )
    }

    composable<EditProfileRoute> { PlaceholderScreen() }
    composable<ShareProfileRoute> { PlaceholderScreen() }
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
