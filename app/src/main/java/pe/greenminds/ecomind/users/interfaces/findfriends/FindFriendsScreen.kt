package pe.greenminds.ecomind.users.interfaces.findfriends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.ErrorText
import pe.greenminds.ecomind.shared.interfaces.theme.PlaceholderGray
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.FriendRelation
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserSearchResult
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.EmptyTabMessage
import pe.greenminds.ecomind.users.interfaces.profile.TabError
import pe.greenminds.ecomind.users.interfaces.profile.TabLoading
import pe.greenminds.ecomind.users.interfaces.profile.TextLink

@Composable
fun FindFriendsScreen(
    onBack: () -> Unit,
    onReply: () -> Unit,
    onFriendsChanged: () -> Unit,
    viewModel: FindFriendsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.friendsChanged.collect { onFriendsChanged() }
    }

    FindFriendsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::search,
        onAdd = viewModel::sendRequest,
        onReply = onReply,
        onBack = onBack
    )
}

@Composable
private fun FindFriendsContent(
    state: FindFriendsUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onAdd: (Long) -> Unit,
    onReply: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.find_friends_title), onBack = onBack)

        SearchField(query = state.query, onQueryChange = onQueryChange)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading && state.results.isEmpty() -> TabLoading()

            state.loadFailed -> TabError(onRetry = onRetry)

            // Frame 36: the search did not match anybody
            state.results.isEmpty() -> EmptyTabMessage(
                title = stringResource(R.string.find_friends_empty_title),
                message = stringResource(R.string.find_friends_empty_message)
            )

            else -> LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.results, key = { it.userId }) { result ->
                    UserRow(
                        result = result,
                        isSending = state.sendingToUserId == result.userId,
                        errorMessage = if (state.errorUserId == result.userId) {
                            state.errorMessage?.let { stringResource(it) }
                        } else {
                            null
                        },
                        onAdd = { onAdd(result.userId) },
                        onReply = onReply
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val label = stringResource(R.string.find_friends_search_label)
    val focusManager = LocalFocusManager.current

    Text(
        text = label,
        style = interTextStyle(13, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(11.dp))
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = interTextStyle(14).copy(color = TextPrimary),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search
        ),
        // The list is already filtered while typing, so the key only hides the keyboard
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp)
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.find_friends_search_placeholder),
                        style = interTextStyle(14),
                        color = PlaceholderGray
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun UserRow(
    result: UserSearchResult,
    isSending: Boolean,
    errorMessage: String?,
    onAdd: () -> Unit,
    onReply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp)
        ) {
            InitialsAvatar(name = result.name)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = interTextStyle(14, FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = stringResource(roleText(result.socialRole)),
                    style = interTextStyle(12),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
            RelationAction(
                name = result.name,
                relation = result.relation,
                isSending = isSending,
                onAdd = onAdd,
                onReply = onReply
            )
        }
        if (errorMessage != null) {
            // The screen reader announces why the request was not sent
            Text(
                text = errorMessage,
                style = interTextStyle(13),
                color = ErrorText,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        HorizontalDivider(thickness = 1.dp, color = RowDivider)
    }
}

// "Friends" and "Sent" are only labels; "Reply" and "Add" are actions
@Composable
private fun RelationAction(
    name: String,
    relation: FriendRelation,
    isSending: Boolean,
    onAdd: () -> Unit,
    onReply: () -> Unit
) {
    when {
        isSending -> {
            val sendingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(20.dp)
                    .semantics { contentDescription = sendingDescription }
            )
        }

        relation == FriendRelation.FRIENDS -> RelationLabel(stringResource(R.string.find_friends_friends))

        relation == FriendRelation.REQUEST_SENT -> RelationLabel(stringResource(R.string.find_friends_sent))

        relation == FriendRelation.REQUEST_RECEIVED -> {
            val description = stringResource(R.string.find_friends_reply_description, name)
            TextLink(
                text = stringResource(R.string.find_friends_reply),
                onClick = onReply,
                modifier = Modifier.semantics { contentDescription = description }
            )
        }

        else -> {
            val description = stringResource(R.string.find_friends_add_description, name)
            TextLink(
                text = stringResource(R.string.find_friends_add),
                onClick = onAdd,
                modifier = Modifier.semantics { contentDescription = description }
            )
        }
    }
}

@Composable
private fun RelationLabel(text: String) {
    Text(
        text = text,
        style = interTextStyle(13, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
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
private fun FindFriendsPreview() {
    val name = stringResource(R.string.app_name)
    EcoMindTheme {
        FindFriendsContent(
            state = FindFriendsUiState(
                isLoading = false,
                results = listOf(
                    UserSearchResult(1L, name, SocialRole.STUDENT, FriendRelation.FRIENDS),
                    UserSearchResult(2L, name, SocialRole.STUDENT, FriendRelation.REQUEST_RECEIVED),
                    UserSearchResult(3L, name, SocialRole.PARENT, FriendRelation.NONE),
                    UserSearchResult(4L, name, SocialRole.STUDENT, FriendRelation.REQUEST_SENT)
                ),
                errorUserId = 3L,
                errorMessage = R.string.friend_requests_retry_notice
            ),
            onQueryChange = {},
            onRetry = {},
            onAdd = {},
            onReply = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FindFriendsNoResultsPreview() {
    EcoMindTheme {
        FindFriendsContent(
            state = FindFriendsUiState(isLoading = false, query = stringResource(R.string.app_name)),
            onQueryChange = {},
            onRetry = {},
            onAdd = {},
            onReply = {},
            onBack = {}
        )
    }
}
