package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.StatusSurface
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.InvitationDetails
import pe.greenminds.ecomind.users.domain.model.InvitationStatus
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.TabError
import pe.greenminds.ecomind.users.interfaces.profile.TabLoading

@Composable
fun InvitationStatusScreen(
    friendshipId: Long,
    onBack: () -> Unit,
    viewModel: InvitationStatusViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(friendshipId) {
        viewModel.loadStatus(friendshipId)
    }

    InvitationStatusContent(
        state = state,
        onRetry = { viewModel.loadStatus(friendshipId) },
        onBack = onBack
    )
}

@Composable
private fun InvitationStatusContent(
    state: InvitationStatusUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val invitation = state.invitation

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Frame 40 calls the screen "Invitation sent"; frames 41 and 42, "Invitation status"
        SubScreenHeader(
            title = stringResource(
                if (invitation?.status == InvitationStatus.AWAITING) {
                    R.string.invitation_status_sent_title
                } else {
                    R.string.invitation_status_title
                }
            ),
            onBack = onBack
        )

        when {
            state.isLoading && invitation == null -> TabLoading()
            state.loadFailed || invitation == null -> TabError(onRetry = onRetry)
            else -> {
                FriendRow(invitation = invitation)
                when (invitation.status) {
                    InvitationStatus.AWAITING -> AwaitingResponse()
                    InvitationStatus.NO_RESPONSE -> CenteredMessage(
                        title = R.string.invitation_status_no_response_title,
                        message = R.string.invitation_status_no_response_message
                    )
                    InvitationStatus.JOINED -> StatusBox(
                        text = R.string.invitation_status_joined,
                        textSizeSp = 17,
                        minHeight = 76,
                        topPadding = 50
                    )
                    InvitationStatus.DECLINED -> DeclinedNotice()
                }
            }
        }
    }
}

@Composable
private fun FriendRow(invitation: InvitationDetails) {
    val subtitle = when (invitation.status) {
        InvitationStatus.AWAITING -> R.string.invitation_status_awaiting
        InvitationStatus.NO_RESPONSE -> R.string.invitation_status_no_response_subtitle
        InvitationStatus.JOINED -> R.string.invitation_status_joined_subtitle
        InvitationStatus.DECLINED -> R.string.friend_requests_status_declined
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .heightIn(min = 60.dp)
    ) {
        InitialsAvatar(name = invitation.name)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invitation.name,
                style = interTextStyle(14, FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = stringResource(subtitle),
                style = interTextStyle(12),
                color = TextSecondary,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        if (invitation.status == InvitationStatus.JOINED) {
            Text(
                text = stringResource(R.string.invitation_status_friend),
                style = interTextStyle(13, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
    HorizontalDivider(
        thickness = 1.dp,
        color = RowDivider,
        modifier = Modifier.padding(top = 20.dp)
    )
}

// Frame 40
@Composable
private fun AwaitingResponse() {
    StatusBox(
        text = R.string.invitation_status_sent_box,
        textSizeSp = 14,
        minHeight = 46,
        topPadding = 38
    )
    Text(
        text = stringResource(R.string.invitation_status_waiting),
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 19.dp)
    )
    Text(
        text = stringResource(R.string.invitation_status_expires),
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 6.dp)
    )
}

// Light green box of frames 40 and 42. Frame 42 also shows "Invitation bonus earned", left out
// because the bonus belongs to Gamification and is not given yet.
@Composable
private fun StatusBox(
    @StringRes text: Int,
    textSizeSp: Int,
    minHeight: Int,
    topPadding: Int
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(top = topPadding.dp)
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .background(StatusSurface, shape)
            .border(1.dp, CardBorder, shape)
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(text),
            style = interTextStyle(textSizeSp, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
    }
}

// Frame 41
@Composable
private fun CenteredMessage(
    @StringRes title: Int,
    @StringRes message: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp)
    ) {
        Text(
            text = stringResource(title),
            style = interTextStyle(17, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(message),
            style = interTextStyle(13),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

// Same texts as the declined request of frame 35
@Composable
private fun DeclinedNotice() {
    Text(
        text = stringResource(R.string.friend_requests_declined_notice),
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 16.dp)
    )
    Text(
        text = stringResource(R.string.friend_requests_retry_notice),
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InvitationAwaitingPreview() {
    StatusPreview(InvitationStatus.AWAITING)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InvitationNoResponsePreview() {
    StatusPreview(InvitationStatus.NO_RESPONSE)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InvitationJoinedPreview() {
    StatusPreview(InvitationStatus.JOINED)
}

@Composable
private fun StatusPreview(status: InvitationStatus) {
    EcoMindTheme {
        InvitationStatusContent(
            state = InvitationStatusUiState(
                isLoading = false,
                invitation = InvitationDetails(
                    friendshipId = 1L,
                    name = stringResource(R.string.app_name),
                    socialRole = SocialRole.STUDENT,
                    status = status
                )
            ),
            onRetry = {},
            onBack = {}
        )
    }
}
