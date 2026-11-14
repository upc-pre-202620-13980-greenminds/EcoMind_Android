package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.quests.presentation.ui.progress.ProgressSectionCard

@Composable
internal fun FamilyPlanProgressSection(
    state: FamilyPlansUiState,
    entries: List<ProgressEntry>,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    onRetry: () -> Unit
) {
    val overview = state.overview ?: return
    if (overview.membership == null) return
    val active = overview.activePlan
    val sessions = active?.items?.mapNotNull { it.collaborativeSessionId }.orEmpty().toSet()
    val currentEntries = entries.filter { it.questUser.collaborativeSessionId in sessions }.take(2)
    ProgressSectionCard(
        label = stringResource(R.string.progress_family_quest), entries = currentEntries,
        onOpenEntry = active?.let { plan -> { _: ProgressEntry -> onOpen(plan.id) } },
        emptyText = when {
            active != null -> null
            overview.isParent -> stringResource(R.string.family_plan_empty)
            else -> stringResource(R.string.family_plan_wait_parent)
        },
        contentAfterEntries = {
            if (state.error != null) FamilyPlanError(onRetry, !state.busy)
            if (active != null) {
                if (currentEntries.isEmpty()) FamilyPlanProgress(active.progress)
                FamilyPlanButton(stringResource(R.string.progress_see_family_quest), { onOpen(active.id) },
                    Modifier.align(Alignment.End).widthIn(max = 180.dp), enabled = !state.busy, compact = true)
            } else if (overview?.isParent == true) {
                FamilyPlanButton(stringResource(if (overview.draftPlan != null) R.string.family_plan_continue else R.string.family_plan_new),
                    onCreate, Modifier.align(Alignment.CenterHorizontally).widthIn(max = 220.dp), enabled = !state.busy)
            }
        }
    )
}
