package pe.greenminds.ecomind.quests.presentation.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import pe.greenminds.ecomind.quests.presentation.viewmodel.FamilyPlansViewModel
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.quests.presentation.ui.family.FamilyPlanProgressSection
import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.shared.infrastructure.offline.isOnline

@Composable
fun ProgressScreen(
    onOpenList: (ProgressSection) -> Unit = {},
    onOpenQuest: (Long) -> Unit = {},
    onCreateFamilyPlan: () -> Unit = {},
    onLoadFamilyPlans: () -> Unit = {},
    onOpenFamilyPlan: (Long) -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel(),
    familyViewModel: FamilyPlansViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val familyState = familyViewModel.state.collectAsStateWithLifecycle().value
    val online = isOnline()
    LaunchedEffect(online) {
        viewModel.setOnline(online)
        if (online) familyViewModel.refresh()
    }
    // Refresh once when returning from another screen, never on a timer.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (online) {
            viewModel.loadProgress()
            familyViewModel.refresh()
        }
    }
    ProgressContent(
        state = state, onRetry = viewModel::loadProgress,
        onRefresh = { viewModel.refreshProgress(); familyViewModel.refresh() },
        onOpenList = onOpenList, onOpenQuest = onOpenQuest, familyState = familyState.takeIf { online },
        online = online,
        onCreateFamilyPlan = onCreateFamilyPlan, onLoadFamilyPlans = onLoadFamilyPlans,
        onOpenFamilyPlan = onOpenFamilyPlan, onRetryFamily = familyViewModel::refresh,
        onOpenEntry = { entry ->
            if (entry.questType == QuestType.FAMILY && online) {
                val plan = familyState.overview?.plans?.find { plan ->
                    plan.items.any { it.collaborativeSessionId != null && it.collaborativeSessionId == entry.questUser.collaborativeSessionId }
                }
                if (plan != null) onOpenFamilyPlan(plan.id) else onLoadFamilyPlans()
            } else onOpenQuest(entry.questId)
        }
    )
}

@Composable
internal fun ProgressContent(
    state: ProgressUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = onRetry,
    onOpenList: (ProgressSection) -> Unit = {},
    onOpenQuest: (Long) -> Unit = {},
    familyState: FamilyPlansUiState? = null,
    onCreateFamilyPlan: () -> Unit = {},
    onLoadFamilyPlans: () -> Unit = {},
    onOpenFamilyPlan: (Long) -> Unit = {},
    onRetryFamily: () -> Unit = {},
    onOpenEntry: ((ProgressEntry) -> Unit)? = null,
    online: Boolean = true
) {
    QuestRefreshContainer(online && state.isRefreshing, onRefresh, enabled = online && !state.isLoading) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            if (online && state.hasError) ProgressLoadError(onRetry, !state.isLoading)
            val noSnapshot = !online && !state.hasLoaded
            if (noSnapshot && !state.isLoading) ProgressOfflineHint()
            if (!noSnapshot || state.inProgress.isNotEmpty()) ProgressSectionCard(
                label = stringResource(R.string.progress_in_progress), entries = state.inProgress.take(2),
                actionLabel = stringResource(R.string.progress_see_more),
                onAction = { onOpenList(ProgressSection.IN_PROGRESS) },
                onOpenEntry = onOpenEntry ?: { onOpenQuest(it.questId) },
                emptyText = if (state.isLoading || noSnapshot) null else stringResource(R.string.progress_empty_active),
                pendingAssignmentIds = state.pendingAssignmentIds
            )
            if (familyState != null) {
                FamilyPlanProgressSection(familyState, state.familyQuests, onCreateFamilyPlan,
                    onOpenFamilyPlan, onRetryFamily)
            } else if (state.familyQuests.isNotEmpty()) {
                ProgressSectionCard(
                    label = stringResource(R.string.progress_family_quest), entries = state.familyQuests.take(2),
                    actionLabel = stringResource(R.string.progress_see_family_quest),
                    onAction = { onOpenList(ProgressSection.FAMILY) },
                    onOpenEntry = onOpenEntry ?: { onOpenQuest(it.questId) }
                )
            }
            if (!noSnapshot || state.finished.isNotEmpty()) ProgressSectionCard(
                label = stringResource(R.string.progress_finished), entries = state.finished.take(2),
                actionLabel = stringResource(R.string.progress_view_history),
                onAction = { onOpenList(ProgressSection.HISTORY) },
                onOpenEntry = onOpenEntry ?: { onOpenQuest(it.questId) },
                emptyText = if (state.isLoading || noSnapshot) null else stringResource(R.string.progress_empty_finished)
            )
        }
        if (!state.isRefreshing && (state.isLoading || online && (state.isUpdating || familyState?.busy == true))) {
            LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter))
        }
    }
}

@Composable
internal fun ProgressOfflineHint() {
    Text(
        stringResource(R.string.offline_progress_missing),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
