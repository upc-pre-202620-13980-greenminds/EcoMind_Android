package pe.greenminds.ecomind.quests.presentation.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.ProgressSection
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestRefreshContainer
import pe.greenminds.ecomind.quests.presentation.viewmodel.ProgressViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun ProgressScreen(
    onOpenList: (ProgressSection) -> Unit = {},
    onOpenQuest: (Long) -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    // Refresh once when returning from another screen, never on a timer.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (viewModel.state.value.hasLoaded) viewModel.loadProgress()
    }
    ProgressContent(
        state = state, onRetry = viewModel::loadProgress, onRefresh = viewModel::refreshProgress,
        onOpenList = onOpenList, onOpenQuest = onOpenQuest
    )
}

@Composable
internal fun ProgressContent(
    state: ProgressUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = onRetry,
    onOpenList: (ProgressSection) -> Unit = {},
    onOpenQuest: (Long) -> Unit = {}
) {
    QuestRefreshContainer(state.isRefreshing, onRefresh, enabled = !state.isLoading) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            if (state.hasError) ProgressLoadError(onRetry, !state.isLoading)
            ProgressSectionCard(
                label = stringResource(R.string.progress_in_progress), entries = state.inProgress.take(2),
                actionLabel = stringResource(R.string.progress_see_more),
                onAction = { onOpenList(ProgressSection.IN_PROGRESS) },
                onOpenEntry = { onOpenQuest(it.questId) },
                emptyText = if (state.isLoading) null else stringResource(R.string.progress_empty_active)
            )
            if (state.familyQuests.isNotEmpty()) {
                ProgressSectionCard(
                    label = stringResource(R.string.progress_family_quest), entries = state.familyQuests.take(2),
                    actionLabel = stringResource(R.string.progress_see_family_quest),
                    onAction = { onOpenList(ProgressSection.FAMILY) },
                    onOpenEntry = { onOpenQuest(it.questId) }
                )
            }
            ProgressSectionCard(
                label = stringResource(R.string.progress_finished), entries = state.finished.take(2),
                actionLabel = stringResource(R.string.progress_view_history),
                onAction = { onOpenList(ProgressSection.HISTORY) },
                onOpenEntry = { onOpenQuest(it.questId) },
                emptyText = if (state.isLoading) null else stringResource(R.string.progress_empty_finished)
            )
        }
    }
}

@Composable
internal fun ProgressLoadError(onRetry: () -> Unit, enabled: Boolean = true) {
    Column {
        Text(stringResource(R.string.progress_load_error), color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry, enabled = enabled) { Text(stringResource(R.string.quest_try_again)) }
    }
}

@Preview(name = "Progress · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Progreso · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProgressContentPreview() {
    val sample = stringResource(R.string.app_name)
    EcoMindTheme {
        ProgressContent(
            state = ProgressUiState(
                isLoading = false,
                inProgress = listOf(previewProgressEntry(1L, sample, 70), previewProgressEntry(2L, sample, 30)),
                familyQuests = listOf(previewProgressEntry(3L, sample, 70), previewProgressEntry(4L, sample, 30)),
                finished = listOf(previewProgressEntry(5L, sample, 100))
            )
        )
    }
}
