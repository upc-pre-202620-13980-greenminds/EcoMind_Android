package pe.greenminds.ecomind.quests.presentation.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.ProgressSection
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyBackButton
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestRefreshContainer
import pe.greenminds.ecomind.quests.presentation.viewmodel.ProgressViewModel
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.quests.presentation.viewmodel.FamilyPlansViewModel
import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.shared.infrastructure.offline.isOnline

@Composable
fun ProgressListScreen(
    section: ProgressSection,
    onBack: () -> Unit,
    onOpenQuest: (Long) -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
    familyViewModel: FamilyPlansViewModel = hiltViewModel(),
    onOpenFamilyPlan: (Long?) -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val familyState = familyViewModel.state.collectAsStateWithLifecycle().value
    val online = isOnline()
    LaunchedEffect(online) {
        viewModel.setOnline(online)
        if (online) familyViewModel.refresh()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (online) {
            viewModel.loadProgress()
            familyViewModel.refresh()
        }
    }
    ProgressListContent(section, state, onBack, onOpenQuest, viewModel::loadProgress, viewModel::refreshProgress,
        onOpenEntry = { entry ->
            if (entry.questType == QuestType.FAMILY && online) {
                val plan = familyState.overview?.plans?.find { plan -> plan.items.any {
                    it.collaborativeSessionId != null && it.collaborativeSessionId == entry.questUser.collaborativeSessionId
                } }
                onOpenFamilyPlan(plan?.id)
            } else onOpenQuest(entry.questId)
        }, online = online)
}

@Composable
internal fun ProgressListContent(
    section: ProgressSection,
    state: ProgressUiState,
    onBack: () -> Unit,
    onOpenQuest: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpenEntry: ((ProgressEntry) -> Unit)? = null,
    online: Boolean = true
) {
    val entries = when (section) {
        ProgressSection.IN_PROGRESS -> state.inProgress
        ProgressSection.FAMILY -> state.familyQuests
        ProgressSection.HISTORY -> state.history
    }
    val title = stringResource(when (section) {
        ProgressSection.IN_PROGRESS -> R.string.progress_in_progress
        ProgressSection.FAMILY -> R.string.progress_family_quest
        ProgressSection.HISTORY -> R.string.progress_history
    })
    QuestRefreshContainer(online && state.isRefreshing, onRefresh, enabled = online && !state.isLoading) {
        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
            PartyBackButton(onBack, Modifier.offset(x = (-12).dp))
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (online && state.hasError) ProgressLoadError(onRetry, !state.isLoading)
                val noSnapshot = !online && !state.hasLoaded
                if (noSnapshot && !state.isLoading) ProgressOfflineHint()
                if (!noSnapshot || entries.isNotEmpty()) ProgressSectionCard(
                    label = title,
                    entries = entries,
                    onOpenEntry = onOpenEntry ?: { onOpenQuest(it.questId) },
                    pendingAssignmentIds = if (section == ProgressSection.IN_PROGRESS) state.pendingAssignmentIds else emptySet(),
                    emptyText = if (state.isLoading || noSnapshot) null else stringResource(when (section) {
                        ProgressSection.IN_PROGRESS -> R.string.progress_empty_active
                        ProgressSection.FAMILY -> R.string.progress_empty_family
                        ProgressSection.HISTORY -> R.string.progress_empty_finished
                    })
                )
            }
        }
    }
}

@Preview(name = "History · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Historial · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProgressListPreview() {
    val water = previewProgressEntry(2L, "Save up water", 100)
    val energy = previewProgressEntry(1L, "Time to save up energy!", 100)
    EcoMindTheme {
        Surface {
            ProgressListContent(
                section = ProgressSection.HISTORY,
                state = ProgressUiState(isLoading = false, history = listOf(
                    water.copy(questUser = water.questUser.copy(endDate = "2026-11-05")),
                    energy.copy(
                        questUser = energy.questUser.copy(endDate = "2026-11-04"),
                        otherCompletions = listOf(
                            energy.questUser.copy(id = 4L, endDate = "2026-11-06"),
                            energy.questUser.copy(id = 3L, endDate = "2026-11-05")
                        )
                    )
                )),
                onBack = {}, onOpenQuest = {}, onRetry = {}, onRefresh = {}
            )
        }
    }
}
