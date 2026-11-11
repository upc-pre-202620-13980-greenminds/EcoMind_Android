package pe.greenminds.ecomind.iam.interfaces.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.preferences.NavigationRow
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesDivider
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesGroup
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesHeader
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesSectionTitle
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesUiState
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesViewModel
import pe.greenminds.ecomind.shared.interfaces.preferences.languageName
import pe.greenminds.ecomind.shared.interfaces.preferences.themeName
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Frame 69. "Account information" (frames 71 and 72) is left out until that screen is built.
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: SettingsViewModel,
    preferencesViewModel: PreferencesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val preferences = preferencesViewModel.state.collectAsStateWithLifecycle().value

    SettingsContent(
        state = state,
        preferences = preferences,
        onBack = onBack,
        onOpenNotifications = onOpenNotifications,
        onOpenLanguage = onOpenLanguage,
        onOpenTheme = onOpenTheme,
        onOpenHelp = onOpenHelp,
        onLogout = viewModel::logout
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    preferences: PreferencesUiState,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        PreferencesHeader(title = stringResource(R.string.top_bar_settings), onBack = onBack)

        PreferencesSectionTitle(stringResource(R.string.settings_preferences))
        PreferencesGroup {
            NavigationRow(
                title = stringResource(R.string.settings_notification_preferences),
                onClick = onOpenNotifications
            )
            PreferencesDivider()
            NavigationRow(
                title = stringResource(R.string.settings_language),
                value = stringResource(languageName(preferences.language)),
                onClick = onOpenLanguage
            )
            PreferencesDivider()
            NavigationRow(
                title = stringResource(R.string.settings_theme),
                value = stringResource(themeName(preferences.theme)),
                onClick = onOpenTheme
            )
        }

        PreferencesSectionTitle(stringResource(R.string.settings_help))
        PreferencesGroup {
            NavigationRow(
                title = stringResource(R.string.settings_help_support),
                onClick = onOpenHelp
            )
        }

        Spacer(modifier = Modifier.height(26.dp))
        LogOutRow(isLoading = state.isLoading, onLogout = onLogout)

        state.errorMessage?.let {
            Text(
                text = stringResource(it),
                style = interTextStyle(13),
                color = EcoMindTheme.colors.errorRed,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Light red row of the design; it cannot be pressed again while the session is closing
@Composable
private fun LogOutRow(
    isLoading: Boolean,
    onLogout: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(EcoMindTheme.colors.errorSurface)
            .border(1.dp, EcoMindTheme.colors.cardBorder, shape)
            .clickable(enabled = !isLoading, role = Role.Button, onClick = onLogout)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(if (isLoading) R.string.logging_out else R.string.log_out),
            style = interTextStyle(14, FontWeight.Bold),
            color = EcoMindTheme.colors.errorRed
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsPreview() {
    EcoMindTheme {
        SettingsContent(
            state = SettingsUiState(),
            preferences = PreferencesUiState(),
            onBack = {},
            onOpenNotifications = {},
            onOpenLanguage = {},
            onOpenTheme = {},
            onOpenHelp = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsDarkPreview() {
    EcoMindTheme(darkTheme = true) {
        SettingsContent(
            state = SettingsUiState(),
            preferences = PreferencesUiState(),
            onBack = {},
            onOpenNotifications = {},
            onOpenLanguage = {},
            onOpenTheme = {},
            onOpenHelp = {},
            onLogout = {}
        )
    }
}
