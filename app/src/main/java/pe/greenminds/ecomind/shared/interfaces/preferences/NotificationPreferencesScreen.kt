package pe.greenminds.ecomind.shared.interfaces.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Frame 70 (TS-008). Frame 83 is the version of a parent, which this app does not have.
@Composable
fun NotificationPreferencesScreen(
    onBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    NotificationPreferencesContent(
        enabledCategories = state.enabledCategories,
        saveError = state.saveError?.let { stringResource(it) },
        onCategoryChanged = viewModel::onCategoryChanged,
        onBack = onBack
    )
}

@Composable
private fun NotificationPreferencesContent(
    enabledCategories: Set<NotificationCategory>,
    saveError: String?,
    onCategoryChanged: (NotificationCategory, Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        PreferencesHeader(
            title = stringResource(R.string.settings_notification_preferences),
            onBack = onBack
        )
        PreferencesDescription(stringResource(R.string.settings_notifications_description))

        PreferencesGroup {
            NotificationCategory.entries.forEachIndexed { index, category ->
                if (index > 0) PreferencesDivider()
                val texts = textsOf(category)
                SwitchRow(
                    title = stringResource(texts.first),
                    description = stringResource(texts.second),
                    checked = category in enabledCategories,
                    onCheckedChange = { enabled -> onCategoryChanged(category, enabled) }
                )
            }
        }
        if (saveError != null) PreferencesSaveError(saveError)
    }
}

// Title and description of each category, as in the design
private fun textsOf(category: NotificationCategory): Pair<Int, Int> = when (category) {
    NotificationCategory.NEW_AND_DAILY_QUESTS ->
        R.string.notifications_quests to R.string.notifications_quests_description
    NotificationCategory.LEARNING_REMINDERS ->
        R.string.notifications_learning to R.string.notifications_learning_description
    NotificationCategory.ACHIEVEMENTS_AND_BADGES ->
        R.string.notifications_achievements to R.string.notifications_achievements_description
    NotificationCategory.WEEKLY_PROGRESS_SUMMARY ->
        R.string.notifications_weekly to R.string.notifications_weekly_description
    NotificationCategory.COMMUNITY_EVENTS ->
        R.string.notifications_events to R.string.notifications_events_description
    NotificationCategory.COMMUNITY_ACTIVITY ->
        R.string.notifications_activity to R.string.notifications_activity_description
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationPreferencesPreview() {
    EcoMindTheme {
        NotificationPreferencesContent(
            enabledCategories = NotificationCategory.entries.toSet() -
                NotificationCategory.WEEKLY_PROGRESS_SUMMARY,
            saveError = null,
            onCategoryChanged = { _, _ -> },
            onBack = {}
        )
    }
}
