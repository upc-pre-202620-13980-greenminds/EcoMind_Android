package pe.greenminds.ecomind.users.interfaces.friendrequests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.ErrorText
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.FriendRequest
import pe.greenminds.ecomind.users.domain.model.FriendRequests
import pe.greenminds.ecomind.users.domain.model.FriendshipStatus
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.TabError
import pe.greenminds.ecomind.users.interfaces.profile.TabLoading
import pe.greenminds.ecomind.users.interfaces.profile.TextLink

@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    onOpenInvitation: (Long) -> Unit,
    onFriendsChanged: () -> Unit,
    viewModel: FriendRequestsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.friendsChanged.collect { onFriendsChanged() }
    }

    FriendRequestsContent(
        state = state,
        onRetry = viewModel::loadRequests,
        onAccept = viewModel::accept,
        onDecline = viewModel::decline,
        onOpenInvitation = onOpenInvitation,
        onBack = onBack
    )
}

@Composable
private fun FriendRequestsContent(
    state: FriendRequestsUiState,
    onRetry: () -> Unit,
    onAccept: (Long) -> Unit,
    onDecline: (Long) -> Unit,
    onOpenInvitation: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val requests = state.requests

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.friend_requests_title), onBack = onBack)

        when {
            state.isLoading && requests == null -> TabLoading()

            state.loadFailed || requests == null -> TabError(onRetry = onRetry)

            else -> {
                // Both sections are always shown, as in frame 34, even when one is empty
                SectionTitle(stringResource(R.string.friend_requests_received, requests.received.size))
                if (requests.received.isEmpty()) {
                    EmptySectionText(stringResource(R.string.friend_requests_received_empty))
                } else {
                    requests.received.forEach { request ->
                        ReceivedRequestRow(
                            request = request,
                            isAnswering = state.answeringId == request.friendshipId,
                            errorMessage = if (state.errorFriendshipId == request.friendshipId) {
                                state.errorMessage?.let { stringResource(it) }
                            } else {
                                null
                            },
                            onAccept = { onAccept(request.friendshipId) },
                            onDecline = { onDecline(request.friendshipId) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                SectionTitle(stringResource(R.string.friend_requests_sent, requests.sent.size))
                if (requests.sent.isEmpty()) {
                    EmptySectionText(stringResource(R.string.friend_requests_sent_empty))
                } else {
                    requests.sent.forEach { request ->
                        SentRequestRow(
                            request = request,
                            onOpen = { onOpenInvitation(request.friendshipId) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = interTextStyle(16, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 10.dp, bottom = 12.dp)
            .semantics { heading() }
    )
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 6.dp)
    )
}

// Avatar, name and a second line, the same for received and sent requests
@Composable
private fun RequestPerson(
    request: FriendRequest,
    subtitle: String,
    trailingLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
            )
    ) {
        InitialsAvatar(name = request.name)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.name,
                style = interTextStyle(14, FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = interTextStyle(12),
                color = TextSecondary,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        if (trailingLabel != null) {
            Text(
                text = trailingLabel,
                style = interTextStyle(13, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
    HorizontalDivider(
        thickness = 1.dp,
        color = RowDivider,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
private fun ReceivedRequestRow(
    request: FriendRequest,
    isAnswering: Boolean,
    errorMessage: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    RequestPerson(request = request, subtitle = stringResource(roleText(request.socialRole)))

    // Accept and Decline go under the divider, aligned to the right
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isAnswering) {
            val answeringDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = answeringDescription }
            )
        } else {
            val acceptDescription = stringResource(R.string.friend_requests_accept_description, request.name)
            val declineDescription = stringResource(R.string.friend_requests_decline_description, request.name)
            TextLink(
                text = stringResource(R.string.friend_requests_accept),
                onClick = onAccept,
                style = interTextStyle(14, FontWeight.Bold),
                modifier = Modifier.semantics { contentDescription = acceptDescription }
            )
            TextLink(
                text = stringResource(R.string.friend_requests_decline),
                onClick = onDecline,
                style = interTextStyle(14, FontWeight.Bold),
                modifier = Modifier
                    .padding(end = 20.dp)
                    .semantics { contentDescription = declineDescription }
            )
        }
    }
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            style = interTextStyle(13),
            color = ErrorText,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

// Frame 34 shows a pending request and frame 35 a declined one
@Composable
private fun SentRequestRow(
    request: FriendRequest,
    onOpen: () -> Unit
) {
    val isDeclined = request.status == FriendshipStatus.REJECTED

    RequestPerson(
        request = request,
        subtitle = stringResource(
            if (isDeclined) R.string.friend_requests_status_declined else R.string.friend_requests_status_pending
        ),
        trailingLabel = if (isDeclined) null else stringResource(R.string.find_friends_sent),
        // Opens the invitation status of frames 40 to 42
        onClick = onOpen
    )
    if (isDeclined) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            Text(
                text = stringResource(R.string.friend_requests_declined_notice),
                style = interTextStyle(13),
                color = TextSecondary
            )
            Text(
                text = stringResource(R.string.friend_requests_retry_notice),
                style = interTextStyle(13),
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun roleText(socialRole: SocialRole): Int {
    return if (socialRole == SocialRole.PARENT) {
        R.string.profile_role_parent
    } else {
        R.string.profile_role_student
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FriendRequestsReceivedPreview() {
    val name = stringResource(R.string.app_name)
    EcoMindTheme {
        FriendRequestsContent(
            state = FriendRequestsUiState(
                isLoading = false,
                requests = FriendRequests(
                    received = listOf(
                        FriendRequest(1L, 5L, name, SocialRole.STUDENT, FriendshipStatus.PENDING)
                    ),
                    sent = emptyList()
                )
            ),
            onRetry = {},
            onAccept = {},
            onDecline = {},
            onOpenInvitation = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FriendRequestsSentPreview() {
    val name = stringResource(R.string.app_name)
    EcoMindTheme {
        FriendRequestsContent(
            state = FriendRequestsUiState(
                isLoading = false,
                requests = FriendRequests(
                    received = emptyList(),
                    sent = listOf(
                        FriendRequest(2L, 6L, name, SocialRole.STUDENT, FriendshipStatus.PENDING),
                        FriendRequest(3L, 7L, name, SocialRole.STUDENT, FriendshipStatus.REJECTED)
                    )
                )
            ),
            onRetry = {},
            onAccept = {},
            onDecline = {},
            onOpenInvitation = {},
            onBack = {}
        )
    }
}
