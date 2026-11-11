package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
import pe.greenminds.ecomind.shared.interfaces.components.BorderedTextField
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.ErrorText
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
fun ChooseContactsScreen(
    onBack: () -> Unit,
    onOpenStatus: (Long) -> Unit,
    onOpenRequests: () -> Unit,
    onFriendsChanged: () -> Unit,
    viewModel: ChooseContactsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.friendsChanged.collect { onFriendsChanged() }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChooseContactsEvent.OpenStatus -> onOpenStatus(event.friendshipId)
                ChooseContactsEvent.OpenRequests -> onOpenRequests()
            }
        }
    }

    ChooseContactsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::search,
        onToggle = viewModel::onToggle,
        onSend = viewModel::sendInvitations,
        onBack = onBack
    )
}

@Composable
private fun ChooseContactsContent(
    state: ChooseContactsUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onToggle: (UserSearchResult) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.choose_contacts_title), onBack = onBack)

        BorderedTextField(
            label = stringResource(R.string.choose_contacts_search_label),
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.choose_contacts_search_placeholder),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Text(
            text = stringResource(R.string.choose_contacts_selected, state.selected.size),
            style = interTextStyle(16, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 20.dp, bottom = 8.dp)
                .semantics { heading() }
        )

        // The list takes the free space and the buttons stay at the bottom, as in the design
        Column(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.contacts.isEmpty() && state.selected.isEmpty() -> TabLoading()

                state.loadFailed -> TabError(onRetry = onRetry)

                state.contacts.isEmpty() && state.selected.isEmpty() -> EmptyTabMessage(
                    title = stringResource(R.string.find_friends_empty_title),
                    message = stringResource(R.string.find_friends_empty_message)
                )

                else -> LazyColumn {
                    items(state.selected, key = { "selected_${it.userId}" }) { contact ->
                        ContactRow(contact = contact, isSelected = true, onToggle = { onToggle(contact) })
                    }
                    items(state.contacts, key = { "contact_${it.userId}" }) { contact ->
                        ContactRow(contact = contact, isSelected = false, onToggle = { onToggle(contact) })
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            // The screen reader announces why some invitations were not sent
            Text(
                text = stringResource(state.errorMessage),
                style = interTextStyle(13),
                color = ErrorText,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        if (state.isSending) {
            val sendingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .semantics { contentDescription = sendingDescription }
            )
        }
        PillButton(
            text = stringResource(R.string.choose_contacts_send),
            onClick = onSend,
            faceColor = EcoGreen,
            baseColor = EcoGreenDark,
            height = 48.dp,
            textStyle = interTextStyle(16, FontWeight.Bold),
            enabled = state.selected.isNotEmpty() && !state.isSending,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        TextLink(
            text = stringResource(R.string.choose_contacts_cancel),
            onClick = onBack,
            style = interTextStyle(13),
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ContactRow(
    contact: UserSearchResult,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val actionDescription = stringResource(
        if (isSelected) R.string.choose_contacts_unselect_description else R.string.choose_contacts_select_description,
        contact.name
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp)
        ) {
            InitialsAvatar(name = contact.name)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = interTextStyle(14, FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = stringResource(
                        if (isSelected) R.string.choose_contacts_selected_contact else R.string.choose_contacts_contact
                    ),
                    style = interTextStyle(12),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            TextLink(
                text = stringResource(
                    if (isSelected) R.string.choose_contacts_selected_action else R.string.choose_contacts_select
                ),
                onClick = onToggle,
                modifier = Modifier.semantics { contentDescription = actionDescription }
            )
        }
        HorizontalDivider(thickness = 1.dp, color = RowDivider)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ChooseContactsPreview() {
    val name = stringResource(R.string.app_name)
    EcoMindTheme {
        ChooseContactsContent(
            state = ChooseContactsUiState(
                isLoading = false,
                selected = listOf(UserSearchResult(1L, name, SocialRole.STUDENT, FriendRelation.NONE)),
                contacts = listOf(UserSearchResult(2L, name, SocialRole.STUDENT, FriendRelation.NONE))
            ),
            onQueryChange = {},
            onRetry = {},
            onToggle = {},
            onSend = {},
            onBack = {}
        )
    }
}
