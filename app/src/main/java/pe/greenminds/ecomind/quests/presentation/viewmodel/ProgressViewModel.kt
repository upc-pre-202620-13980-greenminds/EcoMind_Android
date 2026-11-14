package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.quests.application.usecase.GetProgressOverviewUseCase
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.application.ProgressOverview
import pe.greenminds.ecomind.quests.application.groupQuestCompletions
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuest
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStatus
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStore
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineSnapshots
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressOverview: GetProgressOverviewUseCase,
    private val snapshots: OfflineSnapshots,
    private val offlineQuests: OfflineQuestStore
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    private var savedOverview: ProgressOverview? = null
    private var localQuests = emptyList<OfflineQuest>()
    private var online: Boolean? = null

    init {
        viewModelScope.launch {
            val account = offlineQuests.session() ?: return@launch
            offlineQuests.observe(account.accountId).collect {
                localQuests = it
                publishProgress()
            }
        }
    }

    fun setOnline(connected: Boolean) {
        if (online == connected) return
        online = connected
        loadJob?.cancel()
        loadProgress()
    }

    fun loadProgress() = load(showRefresh = false)
    fun refreshProgress() = load(showRefresh = true)

    private fun load(showRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = !it.hasLoaded, isRefreshing = showRefresh,
                isUpdating = it.hasLoaded, hasError = false) }
            val failed = try {
                val session = snapshots.session()
                if (session != null) {
                    savedOverview = snapshots.read(session, PROGRESS_KEY, ProgressOverview::class.java)
                    publishProgress()
                }
                online == true && snapshots.fetch(PROGRESS_KEY, ProgressOverview::class.java) { account ->
                    getProgressOverview(account.accountId)
                }.onSuccess {
                    savedOverview = it
                }.isFailure
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                online == true
            }
            publishProgress()
            _state.update { it.copy(isLoading = false, isRefreshing = false, isUpdating = false, hasError = failed) }
        }
    }

    private fun publishProgress() {
        val overview = savedOverview
        val completedIds = overview?.history.orEmpty().flatMap { listOf(it.questUser) + it.otherCompletions }
            .mapTo(mutableSetOf()) { it.id }
        val remoteActiveIds = overview?.inProgress.orEmpty().mapTo(mutableSetOf()) { it.questUser.id }
        val pending = localQuests.filter { it.status == OfflineQuestStatus.PENDING }
        val localActive = localQuests.mapNotNull { local ->
            val execution = local.execution
            val assignment = execution.assignment ?: return@mapNotNull null
            if (local.status == OfflineQuestStatus.PENDING ||
                assignment.id !in completedIds &&
                (local.status == OfflineQuestStatus.EDITED ||
                    local.status == OfflineQuestStatus.SAVED && (online != true || assignment.id !in remoteActiveIds)) &&
                assignment.status in setOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
            ) {
                ProgressEntry(assignment, execution.quest.title, questType = execution.quest.type)
            } else null
        }
        val confirmed = localQuests.filter { it.status == OfflineQuestStatus.CONFIRMED }
            .mapNotNull { local ->
                local.execution.assignment?.let { ProgressEntry(it, local.execution.quest.title, questType = local.execution.quest.type) }
            }
        val localIds = (localActive + confirmed).mapTo(mutableSetOf()) { it.questUser.id }
        val pendingIds = pending.mapNotNullTo(mutableSetOf()) { it.execution.assignment?.id }
        val active = (localActive + overview?.inProgress.orEmpty().filter { it.questUser.id !in localIds })
            .sortedByDescending { it.questUser.id }
        _state.update {
            it.copy(
                hasLoaded = overview != null,
                isLoading = it.isLoading && overview == null,
                inProgress = active,
                familyQuests = overview?.familyQuests.orEmpty(),
                finished = mergeHistory(overview?.finished.orEmpty(), confirmed, pendingIds),
                history = mergeHistory(overview?.history.orEmpty(), confirmed, pendingIds),
                pendingAssignmentIds = pendingIds
            )
        }
    }

    private fun mergeHistory(
        history: List<ProgressEntry>,
        confirmed: List<ProgressEntry>,
        pendingIds: Set<Long>
    ): List<ProgressEntry> {
        val entries = history.flatMap { entry ->
            listOf(entry.copy(otherCompletions = emptyList())) +
                entry.otherCompletions.map { entry.copy(questUser = it, otherCompletions = emptyList()) }
        }
        return groupQuestCompletions(
            (entries + confirmed).filter { it.questUser.id !in pendingIds }.distinctBy { it.questUser.id }
        )
    }

    private companion object {
        const val PROGRESS_KEY = "quest-progress"
    }
}
