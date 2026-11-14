package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.application.QuestFilters
import pe.greenminds.ecomind.quests.interfaces.search.QuestSearchBar
import pe.greenminds.ecomind.quests.presentation.ui.filters.QuestFiltersContent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlanEvent
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.quests.presentation.viewmodel.FamilyPlansViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.*

@Composable
fun FamilyPlanBuilderScreen(
    planId: Long?,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onActivated: (Long) -> Unit,
    onViewQuest: (Long) -> Unit,
    viewModel: FamilyPlansViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.family_plan_saved)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (state.details == null) {
            if (planId == null) viewModel.openBuilder() else viewModel.openEditor(planId)
        } else viewModel.refresh()
    }
    LaunchedEffect(viewModel, savedText) {
        viewModel.events.collect { event ->
            if (event is FamilyPlanEvent.Saved) snackbar.showSnackbar(savedText)
        }
    }
    val loadedPlan = state.details?.plan
    LaunchedEffect(loadedPlan?.id, loadedPlan?.status) {
        if (loadedPlan?.status == FamilyPlanStatus.ACTIVE) onActivated(loadedPlan.id)
    }
    Box(Modifier.fillMaxSize()) {
        FamilyPlanBuilderContent(state, onBack, onLoad, viewModel::save, viewModel::activate,
            onRetry = { if (planId == null) viewModel.openBuilder() else viewModel.openEditor(planId) },
            onSearch = viewModel::search,
            onAdd = viewModel::addQuest, onRemove = viewModel::removeQuest, onMove = viewModel::moveQuest,
            onViewQuest = onViewQuest, onQueryChange = viewModel::onQueryChange)
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 66.dp))
    }
}

@Composable
internal fun FamilyPlanBuilderContent(
    state: FamilyPlansUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onSave: () -> Unit,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onSearch: (String, QuestFilters) -> Unit,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onMove: (Long, Int) -> Unit,
    onViewQuest: (Long) -> Unit = {},
    onQueryChange: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchText by rememberSaveable { mutableStateOf(state.query) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(showFilters) { showFilters = false }
    val details = state.details
    Column(Modifier.fillMaxSize().background(White)) {
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            listOf(R.string.family_plan_add_tab, R.string.family_plan_manage_tab).forEachIndexed { index, text ->
                val selected = selectedTab == index
                Column(Modifier.weight(1f).selectable(selected, role = Role.Tab, onClick = { selectedTab = index; showFilters = false }),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(text), color = if (selected) ForestGreen else HintGray,
                        style = interTextStyle(14, FontWeight.SemiBold), modifier = Modifier.padding(vertical = 15.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected) EcoGreen else DividerGray))
                }
            }
        }
        if (state.error != null) FamilyPlanError(onRetry, !state.busy)
        if (details == null) {
            FamilyPlanMessage(stringResource(if (state.busy) R.string.family_plan_loading else if (state.overview?.membership == null)
                R.string.family_plan_requires_family else R.string.family_plan_parent_only), state.busy)
            Spacer(Modifier.weight(1f))
        } else if (!details.canEdit) {
            FamilyPlanMessage(stringResource(R.string.family_plan_parent_only))
            Spacer(Modifier.weight(1f))
        } else {
            if (selectedTab == 0) {
                QuestSearchBar(query = searchText, requestFocus = false,
                    onQueryChange = { searchText = it; onQueryChange(it) },
                    onSearch = { onSearch(searchText, state.filters); showFilters = false },
                    onOpenFilters = { showFilters = !showFilters },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            } else {
                Text(stringResource(R.string.family_plan_title, details.plan.id), style = interTextStyle(15, FontWeight.SemiBold),
                    color = TextPrimary, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp))
            }
            if (selectedTab == 0 && showFilters) {
                QuestFiltersContent(initial = state.filters,
                    onApply = { onSearch(searchText, it); showFilters = false }, modifier = Modifier.weight(1f))
            } else {
            val selectedQuests = details.plan.items.map { it.questId }
            if (selectedTab == 0 && state.searching && state.catalog.isNotEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (selectedTab == 0) {
                    when {
                        state.searching && state.catalog.isEmpty() -> item { FamilyPlanMessage(stringResource(R.string.family_plan_loading), true) }
                        state.searchError != null -> item { FamilyPlanError({ onSearch(state.query, state.filters) }, true) }
                        state.catalog.isEmpty() -> item { FamilyPlanMessage(stringResource(R.string.family_plan_no_results)) }
                        else -> itemsIndexed(state.catalog, key = { _, quest -> quest.id }) { _, quest ->
                            FamilyQuestCard(quest, { onViewQuest(quest.id) }, added = quest.id in state.selectedQuestIds,
                                busy = state.busy || state.searching, onAdd = { onAdd(quest.id) })
                        }
                    }
                } else {
                    if (selectedQuests.isEmpty()) item { FamilyPlanMessage(stringResource(R.string.family_plan_empty_selection)) }
                    itemsIndexed(selectedQuests, key = { _, id -> id }) { index, questId ->
                        val quest = details.quests[questId]
                        if (quest != null) FamilyQuestCard(quest, { onViewQuest(quest.id) }, busy = state.busy,
                            onRemove = { onRemove(questId) }, showTile = false,
                            onMoveUp = if (index > 0) ({ onMove(questId, -1) }) else null,
                            onMoveDown = if (index < selectedQuests.lastIndex) ({ onMove(questId, 1) }) else null)
                        else FamilyPlanMessage(stringResource(R.string.family_plan_unavailable))
                    }
                }
            }
            }
        }
        if (!showFilters) {
        HorizontalDivider(color = CardBorder)
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FamilyPlanButton(stringResource(R.string.family_plan_save), onSave, Modifier.weight(1f), secondary = true,
                enabled = details?.canEdit == true && !state.busy, compact = true)
            FamilyPlanButton(stringResource(R.string.family_plan_start), onStart, Modifier.weight(1f),
                enabled = details?.canActivate == true && !state.busy, compact = true)
            FamilyPlanButton(stringResource(R.string.family_plan_load), onLoad, Modifier.weight(1f), secondary = true,
                enabled = !state.busy, compact = true)
        }
        }
    }
}
