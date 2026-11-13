package pe.greenminds.ecomind.users.interfaces.shareprofile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.ProfileAvatar

@Composable
fun ShareProfileScreen(
    onBack: () -> Unit,
    viewModel: ShareProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    // Placeholder address until EcoMind has a public profile page
    val link = state.userId?.let { stringResource(R.string.share_profile_link, it) }
    val clipLabel = stringResource(R.string.share_profile_title)
    val chooserTitle = stringResource(R.string.share_profile_chooser)

    ShareProfileContent(
        state = state,
        onBack = onBack,
        onCopyLink = {
            if (link != null) {
                copyToClipboard(context, clipLabel, link)
                viewModel.onLinkCopied()
            }
        },
        onSendTo = {
            if (link != null) {
                openShareSheet(context, chooserTitle, link)
            }
        }
    )
}

@Composable
private fun ShareProfileContent(
    state: ShareProfileUiState,
    onBack: () -> Unit,
    onCopyLink: () -> Unit,
    onSendTo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = stringResource(R.string.share_profile_title), onBack = onBack)

        if (state.isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    name = state.name,
                    equippedCosmetics = state.equippedCosmetics,
                    size = 80.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = state.name,
                        style = interTextStyle(18, FontWeight.Bold),
                        color = EcoMindTheme.colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.share_profile_subtitle),
                        style = interTextStyle(13),
                        color = EcoMindTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                PillButton(
                    text = stringResource(R.string.share_profile_copy),
                    onClick = onCopyLink,
                    faceColor = EcoGreen,
                    baseColor = EcoGreenDark,
                    height = 48.dp,
                    textStyle = interTextStyle(16, FontWeight.Bold)
                )
                PillButton(
                    text = stringResource(R.string.share_profile_send),
                    onClick = onSendTo,
                    faceColor = EcoGreen,
                    baseColor = EcoGreenDark,
                    height = 48.dp,
                    textStyle = interTextStyle(16, FontWeight.Bold)
                )
            }

            if (state.linkCopied) {
                Text(
                    text = stringResource(R.string.share_profile_copied),
                    style = interTextStyle(13, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    // The screen reader confirms the copy as soon as it happens
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

// Standard Android share sheet: the user chooses the app that sends the link
private fun openShareSheet(context: Context, title: String, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ShareProfileContentPreview() {
    EcoMindTheme {
        ShareProfileContent(
            state = ShareProfileUiState(
                isLoading = false,
                userId = 1L,
                name = stringResource(R.string.app_name),
                linkCopied = true
            ),
            onBack = {},
            onCopyLink = {},
            onSendTo = {}
        )
    }
}
