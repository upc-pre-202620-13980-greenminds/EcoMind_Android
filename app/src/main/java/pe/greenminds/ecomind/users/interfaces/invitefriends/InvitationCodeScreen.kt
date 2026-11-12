package pe.greenminds.ecomind.users.interfaces.invitefriends

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.InvitationCode
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.TabError
import pe.greenminds.ecomind.users.interfaces.profile.TabLoading

@Composable
fun InvitationCodeScreen(
    onBack: () -> Unit,
    viewModel: InvitationCodeViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    // The code is copied with its space, so it matches what the friend pastes in "Have a code?"
    val shareText = state.code?.let {
        stringResource(R.string.invitation_code_share_text, InvitationCode.display(it))
    }
    val clipLabel = stringResource(R.string.invitation_code_title)

    InvitationCodeContent(
        state = state,
        onCopy = {
            if (shareText != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(clipLabel, shareText))
                viewModel.onCopied()
            }
        },
        onRetry = viewModel::loadCode,
        onBack = onBack
    )
}

@Composable
private fun InvitationCodeContent(
    state: InvitationCodeUiState,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.invitation_code_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.invitation_code_subtitle),
                style = interTextStyle(13),
                color = EcoMindTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 10.dp)
            )
            Spacer(modifier = Modifier.height(51.dp))

            when {
                state.isLoading -> TabLoading()
                state.loadFailed || state.code == null -> TabError(onRetry = onRetry)
                else -> CodeBox(code = state.code)
            }

            if (state.isCopied) {
                Text(
                    text = stringResource(R.string.invitation_code_copied),
                    style = interTextStyle(13, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }

        PillButton(
            text = stringResource(R.string.invitation_code_copy),
            onClick = onCopy,
            faceColor = EcoGreen,
            baseColor = EcoGreenDark,
            height = 48.dp,
            textStyle = interTextStyle(16, FontWeight.Bold),
            enabled = state.code != null,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CodeBox(code: String) {
    val shape = RoundedCornerShape(12.dp)
    val shownCode = InvitationCode.display(code)
    // Read letter by letter, so the screen reader does not try to say the code as a word
    val codeDescription = stringResource(
        R.string.invitation_code_description,
        code.toCharArray().joinToString(" ")
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .background(EcoMindTheme.colors.surfaceTint, shape)
            .border(1.dp, EcoMindTheme.colors.cardBorder, shape)
            .padding(vertical = 16.dp)
            .clearAndSetSemantics { contentDescription = codeDescription }
    ) {
        Text(
            text = stringResource(R.string.invitation_code_label),
            style = interTextStyle(13),
            color = EcoMindTheme.colors.textSecondary
        )
        Text(
            text = shownCode,
            style = interTextStyle(25, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InvitationCodePreview() {
    EcoMindTheme {
        InvitationCodeContent(
            state = InvitationCodeUiState(isLoading = false, code = "K7MP4QXZ", isCopied = true),
            onCopy = {},
            onRetry = {},
            onBack = {}
        )
    }
}
