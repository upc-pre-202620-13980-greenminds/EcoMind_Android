package pe.greenminds.ecomind.settings.interfaces.preferences

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.settings.domain.model.*
import pe.greenminds.ecomind.shared.interfaces.components.SectionHeader
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun SettingsScreen(onBack: () -> Unit, onOpen: (String) -> Unit, onSignedOut: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var confirmLogout by remember { mutableStateOf(false) }
    SettingsPage(stringResource(R.string.settings_title), onBack) {
        SettingsHeading(stringResource(R.string.settings_account))
        SettingsGroup { SettingsRow(stringResource(R.string.settings_account_information), onClick = { onOpen("account") }) }
        SettingsHeading(stringResource(R.string.settings_preferences))
        SettingsGroup {
            SettingsRow(stringResource(R.string.settings_notification_preferences), onClick = { onOpen("notifications") })
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_language), stringResource(if (state.preferences.language == AppLanguage.ENGLISH) R.string.settings_english else R.string.settings_spanish), onClick = { onOpen("language") })
            HorizontalDivider()
            SettingsRow(stringResource(R.string.settings_theme), stringResource(if (state.preferences.theme == AppTheme.LIGHT) R.string.settings_light else R.string.settings_dark), onClick = { onOpen("theme") })
        }
        SettingsHeading(stringResource(R.string.settings_help))
        SettingsGroup { SettingsRow(stringResource(R.string.settings_support), onClick = { onOpen("help") }) }
        Spacer(Modifier.height(24.dp))
        TextButton({ confirmLogout = true }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))) {
            Text(stringResource(R.string.settings_log_out), color = MaterialTheme.colorScheme.onErrorContainer, style = interTextStyle(14, FontWeight.Bold))
        }
        if (state.hasError) Text(stringResource(R.string.settings_save_error), color = MaterialTheme.colorScheme.error)
    }
    if (confirmLogout) AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text(stringResource(R.string.settings_log_out)) }, text = { Text(stringResource(R.string.settings_logout_message)) },
        confirmButton = { TextButton({ confirmLogout = false; viewModel.signOut(onSignedOut) }) { Text(stringResource(R.string.settings_log_out)) } }, dismissButton = { TextButton({ confirmLogout = false }) { Text(stringResource(R.string.settings_cancel)) } })
}

@Composable
fun NotificationPreferencesScreen(onBack: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    NotificationPreferencesContent(state, onBack, viewModel::notification)
}

@Composable
internal fun NotificationPreferencesContent(state: PreferencesUiState, onBack: () -> Unit, onChanged: (NotificationCategory, Boolean) -> Unit) {
    SettingsPage(stringResource(R.string.settings_notification_preferences), onBack) {
        Text(stringResource(R.string.settings_notifications_description), style = interTextStyle(13), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.settings_local_preferences), style = interTextStyle(11), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
        if (state.isLoading) CircularProgressIndicator()
        else SettingsGroup {
            NotificationCategory.entries.forEachIndexed { index, category ->
                val labels = categoryLabels(category)
                Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).toggleable(category in state.preferences.enabledNotifications, enabled = !state.isSaving, role = Role.Switch) { onChanged(category, it) }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(stringResource(labels.first), style = interTextStyle(14, FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(labels.second), style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = category in state.preferences.enabledNotifications, onCheckedChange = null, enabled = !state.isSaving, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
                }
                if (index < NotificationCategory.entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        if (state.hasError) Text(stringResource(R.string.settings_save_error), color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
}

@Composable
fun AppearanceScreen(language: Boolean, onBack: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    SettingsPage(stringResource(if (language) R.string.settings_language else R.string.settings_theme), onBack) {
        Text(stringResource(if (language) R.string.settings_language_description else R.string.settings_theme_description), style = interTextStyle(13), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        SettingsGroup {
            if (language) AppLanguage.entries.forEach { value -> RadioRow(stringResource(if (value == AppLanguage.ENGLISH) R.string.settings_english else R.string.settings_spanish), state.preferences.language == value, !state.isSaving) { viewModel.language(value) } }
            else AppTheme.entries.forEach { value -> RadioRow(stringResource(if (value == AppTheme.LIGHT) R.string.settings_light else R.string.settings_dark), state.preferences.theme == value, !state.isSaving) { viewModel.theme(value) } }
        }
        if (state.hasError) Text(stringResource(R.string.settings_save_error), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    SettingsPage(stringResource(R.string.settings_support), onBack) {
        SettingsHeading(stringResource(R.string.settings_about))
        Text(stringResource(R.string.settings_about_body), style = interTextStyle(14))
        SettingsHeading(stringResource(R.string.settings_recognition))
        Text(stringResource(R.string.settings_recognition_body), style = interTextStyle(14))
        SettingsHeading(stringResource(R.string.settings_privacy))
        Text(stringResource(R.string.settings_privacy_body), style = interTextStyle(14))
    }
}

@Composable
internal fun SettingsPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SectionHeader(title, onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp), content = content)
    }
}
@Composable internal fun SettingsHeading(title: String) { Text(title, Modifier.padding(top = 20.dp, bottom = 12.dp).semantics { heading() }, style = interTextStyle(16, FontWeight.Bold), color = MaterialTheme.colorScheme.primary) }
@Composable internal fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp)), content = content)
}
@Composable private fun SettingsRow(title: String, value: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button, onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = interTextStyle(14), color = MaterialTheme.colorScheme.onSurface)
        if (value != null) Text(value, style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("›", Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.primary)
    }
}
@Composable private fun RadioRow(title: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = interTextStyle(14, if (selected) FontWeight.Bold else FontWeight.Normal))
        RadioButton(selected, null, enabled = enabled)
    }
}
private fun categoryLabels(category: NotificationCategory): Pair<Int, Int> = when (category) {
    NotificationCategory.QUESTS -> R.string.pref_quests to R.string.pref_quests_description
    NotificationCategory.LEARNING -> R.string.pref_learning to R.string.pref_learning_description
    NotificationCategory.ACHIEVEMENTS -> R.string.pref_achievements to R.string.pref_achievements_description
    NotificationCategory.WEEKLY_SUMMARY -> R.string.pref_weekly to R.string.pref_weekly_description
    NotificationCategory.EVENTS -> R.string.pref_events to R.string.pref_events_description
    NotificationCategory.COMMUNITY -> R.string.pref_community to R.string.pref_community_description
}
