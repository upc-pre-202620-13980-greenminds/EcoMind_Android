package pe.greenminds.ecomind.users.interfaces.friendprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
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
import pe.greenminds.ecomind.shared.interfaces.theme.ErrorRed
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.FriendProfile
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.TabError
import pe.greenminds.ecomind.users.interfaces.profile.TabLoading
import pe.greenminds.ecomind.users.interfaces.profile.TextLink

@Composable
fun FriendProfileScreen(
    friendId: Long,
    onBack: () -> Unit,
    onRemoved: () -> Unit,
    viewModel: FriendProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(friendId) {
        viewModel.loadProfile(friendId)
    }
    LaunchedEffect(state.isRemoved) {
        if (state.isRemoved) {
            onRemoved()
        }
    }

    FriendProfileContent(
        state = state,
        onRetry = { viewModel.loadProfile(friendId) },
        onRemoveRequested = viewModel::onRemoveRequested,
        onRemoveConfirmed = viewModel::confirmRemoval,
        onRemoveCancelled = viewModel::onRemoveCancelled,
        onBack = onBack
    )
}

@Composable
private fun FriendProfileContent(
    state: FriendProfileUiState,
    onRetry: () -> Unit,
    onRemoveRequested: () -> Unit,
    onRemoveConfirmed: () -> Unit,
    onRemoveCancelled: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.profile_title), onBack = onBack)

        when {
            state.isLoading && profile == null -> TabLoading()
            state.loadFailed || profile == null -> TabError(onRetry = onRetry)
            else -> {
                FriendHeader(profile = profile)

                // A user who is not a friend only sees the data, without the friendship actions
                if (profile.friendshipId != null) {
                    if (state.isConfirmingRemoval) {
                        RemoveFriendCard(
                            name = profile.name,
                            isRemoving = state.isRemoving,
                            errorMessage = state.removeError?.let { stringResource(it) },
                            onConfirm = onRemoveConfirmed,
                            onCancel = onRemoveCancelled
                        )
                    } else {
                        FriendshipActions(onRemoveRequested = onRemoveRequested)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FriendHeader(profile: FriendProfile) {
    InitialsAvatar(
        name = profile.name,
        size = 76.dp,
        modifier = Modifier.padding(top = 6.dp)
    )
    Text(
        text = profile.name,
        style = interTextStyle(22, FontWeight.Bold),
        color = TextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(top = 14.dp)
            .semantics { heading() }
    )
    Text(
        text = stringResource(
            if (profile.socialRole == SocialRole.PARENT) {
                R.string.profile_role_parent
            } else {
                R.string.profile_role_student
            }
        ),
        style = interTextStyle(13),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 9.dp)
    )

    // Unlike the own profile, a friend does not see the gems
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
    ) {
        FriendStat(
            value = profile.streak,
            label = stringResource(R.string.profile_stat_streak),
            modifier = Modifier.weight(1f)
        )
        FriendStat(
            value = profile.ecopoints,
            label = stringResource(R.string.profile_stat_ecopoints),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FriendStat(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // The screen reader says "3, Day streak" as one item
        modifier = modifier.clearAndSetSemantics { contentDescription = "$value, $label" }
    ) {
        Text(
            text = value.toString(),
            style = interTextStyle(24, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = interTextStyle(12),
            color = TextSecondary,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

// Frame 13
@Composable
private fun FriendshipActions(onRemoveRequested: () -> Unit) {
    Text(
        text = stringResource(R.string.friend_profile_you_are_friends),
        style = interTextStyle(13),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 37.dp)
    )
    TextLink(
        text = stringResource(R.string.friend_profile_remove),
        onClick = onRemoveRequested,
        style = interTextStyle(14, FontWeight.Bold),
        color = ErrorRed,
        modifier = Modifier.padding(top = 12.dp)
    )
}

// Frame 14: the confirmation is a card inside the screen, not a dialog
@Composable
private fun RemoveFriendCard(
    name: String,
    isRemoving: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .padding(top = 136.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, CardBorder, shape)
            .padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 8.dp)
            // The screen reader announces the question as soon as the card appears
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            text = stringResource(R.string.friend_profile_remove_title, name),
            style = interTextStyle(18, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.friend_profile_remove_message),
            style = interTextStyle(13),
            color = TextSecondary,
            modifier = Modifier.padding(top = 15.dp)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = interTextStyle(13),
                color = ErrorRed,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 21.dp)
        ) {
            if (isRemoving) {
                val removingDescription = stringResource(R.string.loading)
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(24.dp)
                        .semantics { contentDescription = removingDescription }
                )
            } else {
                TextLink(
                    text = stringResource(R.string.friend_profile_remove),
                    onClick = onConfirm,
                    style = interTextStyle(14, FontWeight.Bold),
                    color = ErrorRed
                )
            }
            TextLink(
                text = stringResource(R.string.friend_profile_cancel),
                onClick = onCancel,
                style = interTextStyle(14, FontWeight.Bold)
            )
        }
    }
}

private fun previewState(isConfirming: Boolean, removeError: Int? = null) = FriendProfileUiState(
    isLoading = false,
    profile = FriendProfile(
        userId = 3L,
        name = "EcoMind",
        socialRole = SocialRole.STUDENT,
        streak = 3,
        ecopoints = 24,
        friendshipId = 1L
    ),
    isConfirmingRemoval = isConfirming,
    removeError = removeError
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FriendProfilePreview() {
    EcoMindTheme {
        FriendProfileContent(
            state = previewState(isConfirming = false),
            onRetry = {},
            onRemoveRequested = {},
            onRemoveConfirmed = {},
            onRemoveCancelled = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RemoveFriendPreview() {
    EcoMindTheme {
        FriendProfileContent(
            state = previewState(isConfirming = true),
            onRetry = {},
            onRemoveRequested = {},
            onRemoveConfirmed = {},
            onRemoveCancelled = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RemoveFriendErrorPreview() {
    EcoMindTheme {
        FriendProfileContent(
            state = previewState(isConfirming = true, removeError = R.string.friend_profile_remove_failed),
            onRetry = {},
            onRemoveRequested = {},
            onRemoveConfirmed = {},
            onRemoveCancelled = {},
            onBack = {}
        )
    }
}
