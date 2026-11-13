package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanEvent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestRefreshContainer
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyBackButton
import pe.greenminds.ecomind.quests.presentation.viewmodel.FamilyPlansViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.*
import pe.greenminds.ecomind.users.domain.model.FamilyRole

@Composable
fun FamilyPlanDetailScreen(
    planId: Long, onBack: () -> Unit, onOpenActivities: (Long, Long) -> Unit,
    onEdit: () -> Unit, viewModel: FamilyPlansViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleting by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (state.details == null) viewModel.openPlan(planId) else viewModel.refresh()
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FamilyPlanEvent.Deleted -> onBack()
                else -> Unit
            }
        }
    }
    FamilyPlanDetailContent(state, onBack, onOpenActivities, onEdit, viewModel::complete,
        onDelete = { viewModel.dismissError(); deleting = true }, onRefresh = { viewModel.openPlan(planId) })
    if (deleting) FamilyPlanDeleteDialog(state.details?.plan?.status == FamilyPlanStatus.ACTIVE, state.busy,
        state.error != null, { deleting = false; viewModel.dismissError() }, viewModel::delete)
    if (state.completedAchievements != null) AlertDialog(onDismissRequest = viewModel::dismissCompletion, containerColor = White,
        title = { Text(stringResource(R.string.family_plan_completion_title)) },
        text = {
            Column {
                pe.greenminds.ecomind.quests.presentation.ui.execution.EcoMascot(
                    confetti = true, modifier = Modifier.align(Alignment.CenterHorizontally).size(160.dp)
                )
                Text(stringResource(R.string.family_plan_completion_body))
                state.completedAchievements.orEmpty().forEach { name -> Text(name, fontWeight = FontWeight.Bold) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::dismissCompletion) { Text(stringResource(R.string.family_plan_close)) } })
}

@Composable
internal fun FamilyPlanDetailContent(
    state: FamilyPlansUiState, onBack: () -> Unit, onOpenActivities: (Long, Long) -> Unit, onEdit: () -> Unit,
    onComplete: () -> Unit, onDelete: () -> Unit, onRefresh: () -> Unit
) {
    val details = state.details
    Column(Modifier.fillMaxSize().background(White)) {
        PartyBackButton(onBack)
        if (state.error != null) FamilyPlanError(onRefresh, !state.busy)
        if (details == null) {
            FamilyPlanMessage(stringResource(R.string.family_plan_loading), state.busy)
            return@Column
        }
        Box(Modifier.weight(1f)) {
        QuestRefreshContainer(state.busy, onRefresh, !state.busy) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.family_plan_title, details.plan.id),
                            style = interTextStyle(24, FontWeight.Bold), color = ForestGreen)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(familyPlanStatus(details.plan.status), style = interTextStyle(14, FontWeight.SemiBold), color = ForestGreen)
                                Text(stringResource(R.string.family_plan_completed_count, details.plan.completedItems, details.plan.items.size),
                                    style = interTextStyle(13), modifier = Modifier.padding(top = 6.dp))
                            }
                            if (details.canDelete) IconButton(onClick = onDelete, enabled = !state.busy) {
                                Icon(painterResource(R.drawable.ic_family_delete), stringResource(if (details.plan.status == FamilyPlanStatus.ACTIVE)
                                    R.string.family_plan_cancel else R.string.family_plan_delete), Modifier.size(22.dp))
                            }
                        }
                        FamilyPlanProgress(details.plan.progress)
                        if (details.canEdit) FamilyPlanButton(stringResource(R.string.family_plan_continue), onEdit, enabled = !state.busy)
                    }
                }
                items(details.plan.items, key = { it.id }) { item ->
                    val quest = details.quests[item.questId]
                    Surface(shape = RoundedCornerShape(12.dp), shadowElevation = 2.dp, color = White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(quest?.title ?: stringResource(R.string.family_plan_unavailable), style = interTextStyle(16, FontWeight.SemiBold))
                            quest?.let { Text(it.description, style = interTextStyle(13), color = DarkGrayText) }
                            FamilyPlanProgress(item.progress)
                            if (details.plan.status in listOf(FamilyPlanStatus.ACTIVE, FamilyPlanStatus.COMPLETED)) {
                                FamilyPlanButton(stringResource(R.string.quest_view), { onOpenActivities(item.questId, item.id) }, blue = true,
                                    enabled = quest != null && item.collaborativeSessionId != null && !state.busy)
                            }
                        }
                    }
                }
            }
        }
        }
        if (details.membership.familyRole == FamilyRole.PARENT && details.plan.status == FamilyPlanStatus.ACTIVE) {
            FamilyPlanButton(stringResource(R.string.family_plan_complete), onComplete,
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                enabled = details.canComplete && !state.busy)
        }
    }
}
