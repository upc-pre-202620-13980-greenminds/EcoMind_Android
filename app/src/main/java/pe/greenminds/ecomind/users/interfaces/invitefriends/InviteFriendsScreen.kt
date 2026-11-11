package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader

@Composable
fun InviteFriendsScreen(
    onBack: () -> Unit,
    onChooseContacts: () -> Unit,
    onShowCode: () -> Unit,
    onRequestSent: (Long) -> Unit,
    viewModel: InviteFriendsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.requestSent.collect { friendshipId -> onRequestSent(friendshipId) }
    }

    InviteFriendsContent(
        state = state,
        onBack = onBack,
        onChooseContacts = onChooseContacts,
        onShowCode = onShowCode,
        onCodeChange = viewModel::onCodeChange,
        onRedeemCode = viewModel::redeemCode
    )
}

@Composable
private fun InviteFriendsContent(
    state: InviteFriendsUiState,
    onBack: () -> Unit,
    onChooseContacts: () -> Unit,
    onShowCode: () -> Unit,
    onCodeChange: (String) -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.invite_friends_title), onBack = onBack)

        Text(
            text = stringResource(R.string.invite_friends_subtitle),
            style = interTextStyle(13),
            color = TextSecondary,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(41.dp))
        MethodCard(
            title = stringResource(R.string.invite_friends_contacts),
            description = stringResource(R.string.invite_friends_contacts_hint),
            onClick = onChooseContacts
        )
        Spacer(modifier = Modifier.height(16.dp))
        MethodCard(
            title = stringResource(R.string.invite_friends_code),
            description = stringResource(R.string.invite_friends_code_hint),
            onClick = onShowCode
        )

        // Not in the Figma: the backend accepts a code, so the friend needs a place to write it.
        // It is kept apart from the two methods above with a divider and its own title.
        HorizontalDivider(
            thickness = 1.dp,
            color = RowDivider,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )
        Text(
            text = stringResource(R.string.invite_code_redeem_title),
            style = interTextStyle(16, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .semantics { heading() }
        )
        BorderedTextField(
            label = stringResource(R.string.invite_code_redeem_label),
            value = state.code,
            onValueChange = onCodeChange,
            placeholder = stringResource(R.string.invite_code_redeem_placeholder),
            errorMessage = state.codeError?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    focusManager.clearFocus()
                    onRedeemCode()
                }
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        PillButton(
            text = stringResource(R.string.invite_code_redeem_send),
            onClick = onRedeemCode,
            faceColor = EcoGreen,
            baseColor = EcoGreenDark,
            height = 48.dp,
            textStyle = interTextStyle(16, FontWeight.Bold),
            enabled = state.code.isNotBlank() && !state.isSending,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        if (state.isSending) {
            val sendingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
                    .semantics { contentDescription = sendingDescription }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// White card with a border: the whole card is the touch target
@Composable
private fun MethodCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, CardBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = interTextStyle(15, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = interTextStyle(12),
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun InviteFriendsPreview() {
    EcoMindTheme {
        InviteFriendsContent(
            state = InviteFriendsUiState(code = "K7MP 4QXZ", codeError = R.string.invite_code_not_found),
            onBack = {},
            onChooseContacts = {},
            onShowCode = {},
            onCodeChange = {},
            onRedeemCode = {}
        )
    }
}
