package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.BorderStroke
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
import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanEvent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestRefreshContainer
import pe.greenminds.ecomind.quests.presentation.viewmodel.FamilyPlansViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.*

@Composable
fun FamilyPlanListScreen(onBack: () -> Unit, onLoad: (FamilyPlan, Boolean) -> Unit, viewModel: FamilyPlansViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val overview = state.overview
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { if (it is FamilyPlanEvent.Deleted) pendingDelete = null }
    }
    FamilyPlanListContent(state, onBack, { plan ->
        onLoad(plan, overview?.isParent == true && overview.membership?.userId == plan.ownerUserId)
    }, { viewModel.dismissError(); pendingDelete = it }, viewModel::refresh)
    pendingDelete?.let { id ->
        FamilyPlanDeleteDialog(active = state.overview?.plans?.find { it.id == id }?.status == FamilyPlanStatus.ACTIVE,
            busy = state.busy, hasError = state.error != null,
            onDismiss = { pendingDelete = null; viewModel.dismissError() }, onConfirm = { viewModel.deletePlan(id) })
    }
}

@Composable
internal fun FamilyPlanListContent(
    state: FamilyPlansUiState, onBack: () -> Unit, onLoad: (FamilyPlan) -> Unit,
    onDelete: (Long) -> Unit, onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(White)) {
        FamilyPlanHeader(stringResource(R.string.family_plan_load_title), onBack)
        if (state.error != null) FamilyPlanError(onRefresh, !state.busy)
        QuestRefreshContainer(state.busy && state.overview != null, onRefresh, !state.busy) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val plans = state.overview?.plans.orEmpty().sortedByDescending { it.id }
                if (plans.isEmpty()) item {
                    FamilyPlanMessage(stringResource(if (state.busy) R.string.family_plan_loading_saved else R.string.family_plan_no_saved), state.busy)
                }
                items(plans, key = { it.id }) { plan ->
                    Surface(shape = RoundedCornerShape(10.dp), color = White, shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, CardBorder)) {
                        Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 8.dp, bottom = 12.dp, end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.family_plan_title, plan.id), style = interTextStyle(15, FontWeight.SemiBold))
                                Text(stringResource(R.string.family_plan_count, plan.items.size), style = interTextStyle(12),
                                    modifier = Modifier.padding(top = 8.dp))
                                Text(familyPlanStatus(plan.status), style = interTextStyle(12), color = DarkGrayText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (state.overview?.isParent == true && state.overview.membership?.userId == plan.ownerUserId &&
                                    plan.status in listOf(FamilyPlanStatus.DRAFT, FamilyPlanStatus.ACTIVE)) {
                                    IconButton(onClick = { onDelete(plan.id) }, enabled = !state.busy) {
                                        Icon(painterResource(R.drawable.ic_family_delete), stringResource(R.string.family_plan_delete),
                                            Modifier.size(20.dp), tint = TextPrimary)
                                    }
                                }
                                FamilyPlanButton(stringResource(R.string.family_plan_load), { onLoad(plan) },
                                    Modifier.width(98.dp), secondary = true, enabled = !state.busy, compact = true)
                            }
                        }
                    }
                }
            }
        }
    }
}
