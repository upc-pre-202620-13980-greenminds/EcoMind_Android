package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.SurfaceTint
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.interfaces.navigation.FindFriendsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FriendProfileRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FriendRequestsRoute
import pe.greenminds.ecomind.users.interfaces.navigation.InviteFriendsRoute

@Composable
fun FriendsTabContent(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onNavigate: (Any) -> Unit
) {
    val overview = state.friends

    when {
        state.isLoadingFriends -> TabLoading()

        state.friendsFailed || overview == null -> TabError(onRetry = onRetry)

        else -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.profile_friends_count,
                        overview.friends.size,
                        overview.friends.size
                    ),
                    style = interTextStyle(16, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                TextLink(
                    text = stringResource(R.string.profile_invite_friends),
                    onClick = { onNavigate(InviteFriendsRoute) },
                    modifier = Modifier.padding(end = 12.dp)
                )
                TextLink(
                    text = stringResource(R.string.profile_find_friends),
                    onClick = { onNavigate(FindFriendsRoute) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            FriendRequestsRow(
                pendingRequests = overview.pendingRequests,
                onClick = { onNavigate(FriendRequestsRoute) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (overview.friends.isEmpty()) {
                EmptyTabMessage(
                    title = stringResource(R.string.profile_friends_empty_title),
                    message = stringResource(R.string.profile_friends_empty_message)
                )
            } else {
                overview.friends.forEach { friend ->
                    PersonRow(
                        name = friend.name,
                        subtitle = stringResource(
                            if (friend.socialRole == SocialRole.PARENT) {
                                R.string.profile_role_parent
                            } else {
                                R.string.profile_role_student
                            }
                        ),
                        onClick = { onNavigate(FriendProfileRoute(friend.userId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRequestsRow(
    pendingRequests: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(SurfaceTint)
            .border(1.dp, CardBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_friend_requests),
            style = interTextStyle(14),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = pendingRequests.toString(),
            style = interTextStyle(14),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
