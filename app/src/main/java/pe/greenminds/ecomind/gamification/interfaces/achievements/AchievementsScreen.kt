package pe.greenminds.ecomind.gamification.interfaces.achievements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.AchievementCollection
import pe.greenminds.ecomind.shared.interfaces.components.SegmentedTabs
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    onOpenAchievement: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    LifecycleResumeEffect(Unit) {
        viewModel.loadAchievements()
        onPauseOrDispose { }
    }
    AchievementsContent(viewModel.state.collectAsStateWithLifecycle().value,
        onBack, onOpenAchievement, viewModel::loadAchievements, onSignIn)
}

@Composable
internal fun AchievementsContent(
    state: AchievementsUiState,
    onBack: () -> Unit,
    onOpenAchievement: (String) -> Unit,
    onRetry: () -> Unit,
    onSignIn: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.achievements_title), onBack)
        CollectionState(state, onRetry, onSignIn) { collection ->
            if (collection.isSimulated) DemoLabel()
            SegmentedTabs(
                options = listOf(stringResource(R.string.achievements_earned), stringResource(R.string.achievements_available)),
                selectedIndex = selectedTab,
                onOptionSelected = { selectedTab = it }
            )
            val entries = if (selectedTab == 0) collection.earned else collection.available
            Text(pluralStringResource(R.plurals.achievements_count, entries.size, entries.size),
                style = interTextStyle(16, FontWeight.Bold), color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp).semantics { heading() })
            if (entries.isEmpty()) {
                StateMessage(stringResource(if (selectedTab == 0) R.string.achievements_empty else R.string.achievements_no_available))
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(entries, key = { it.achievement.id }) { entry ->
                        AchievementRow(entry, onClick = { onOpenAchievement(entry.achievement.id) })
                        HorizontalDivider(color = RowDivider)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CollectionState(
    state: AchievementsUiState,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    content: @Composable ColumnScope.(AchievementCollection) -> Unit
) {
    when {
        state.isLoading -> LoadingContent()
        state.hasError || state.collection == null -> RecoveryContent(state.sessionRequired, onRetry, onSignIn)
        else -> content(state.collection)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AchievementsEmptyPreview() {
    EcoMindTheme {
        AchievementsContent(AchievementsUiState(isLoading = false,
            collection = AchievementCollection(emptyList(), true)), {}, {}, {}, {})
    }
}
