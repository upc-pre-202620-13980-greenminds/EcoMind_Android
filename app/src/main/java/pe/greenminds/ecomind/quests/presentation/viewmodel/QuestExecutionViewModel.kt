package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.*
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.greenminds.ecomind.quests.application.service.*
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.domain.valueobject.QuestPublicationStatus
import pe.greenminds.ecomind.quests.domain.model.PartyFriend
import pe.greenminds.ecomind.quests.application.usecase.GetCollabQuestStateByQuestUseCase
import pe.greenminds.ecomind.quests.application.usecase.GetCollabQuestInvitationUseCase
import pe.greenminds.ecomind.quests.presentation.states.QuestPartyUiState
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestDetailRoute

enum class QuestStage { DETAIL, STARTING, ACTIVITIES, FINISHED }
data class QuestExecutionState(
    val execution: QuestExecution? = null,
    val stage: QuestStage = QuestStage.DETAIL,
    val busy: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val partyState: QuestPartyUiState = QuestPartyUiState(),
    val selectingFriends: Boolean = false,
    val friends: List<PartyFriend> = emptyList()
)

@HiltViewModel
class QuestExecutionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val service: QuestExecutionService,
    private val getPartyState: GetCollabQuestStateByQuestUseCase,
    private val getInvitations: GetCollabQuestInvitationUseCase,
    private val partyActions: QuestPartyService
) : ViewModel() {
    private val questId = savedStateHandle.toRoute<QuestDetailRoute>().questId
    private val mutable = MutableStateFlow(QuestExecutionState())
    val state = mutable.asStateFlow()

    init { reload() }

    private suspend fun loadDetail(previous: QuestExecutionState): QuestExecutionState {
        val loaded = service.load(questId)
        val partyState = if (loaded.quest.type == QuestType.COLLABORATIVE) {
            val party = try {
                Result.success(partyActions.resolveEnrolledState(questId, getPartyState(questId).getOrThrow()))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
            val invitations = getInvitations()
            val received = if (invitations.isSuccess) invitations.getOrThrow().filter { it.questId == questId }
                else previous.partyState.invitations
            val currentParty = if (party.isSuccess) party.getOrThrow() else previous.partyState.party
            val peopleIds = currentParty?.members.orEmpty().map { it.userId }.toSet() + received.map { it.organizerUserId }
            val names = previous.partyState.names + partyActions.names(peopleIds - previous.partyState.names.keys)
            QuestPartyUiState(
                party = currentParty, invitations = received, names = names,
                hasError = party.isFailure || invitations.isFailure,
                hasActiveAssignment = loaded.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE),
                canExecute = loaded.supported,
                canCreateParty = loaded.quest.publicationStatus == QuestPublicationStatus.PUBLISHED && loaded.activities.isNotEmpty()
                    && loaded.assignment?.status !in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
            )
        } else QuestPartyUiState()
        return previous.copy(execution = loaded, partyState = partyState)
    }

    fun reload() = perform { loadDetail(this) }

    fun refreshScreen() = perform(refreshing = true) {
        val updated = loadDetail(this)
        if (selectingFriends) updated.copy(friends = partyActions.friends()) else updated
    }

    fun start() = perform {
        val execution = service.start(requireNotNull(execution))
        copy(execution = execution, stage = QuestStage.STARTING)
    }

    fun startPartyOrSolo() = perform {
        val fresh = loadDetail(this)
        check(!fresh.partyState.hasError)
        val current = requireNotNull(fresh.execution)
        check(current.supported)
        val active = current.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
        val started = if (active) {
            current
        } else if (fresh.partyState.isOwner) {
            check(fresh.partyState.party?.permissions?.canStart == true)
            partyActions.start(requireNotNull(fresh.partyState.party?.session?.id))
            service.load(questId)
        } else {
            check(!fresh.partyState.isEnrolled)
            check(fresh.partyState.canCreateParty)
            service.start(current)
        }
        check(started.supported)
        check(started.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE))
        fresh.copy(execution = started, stage = QuestStage.STARTING)
    }

    fun openFriends() = perform {
        var fresh = loadDetail(this)
        check(!fresh.partyState.hasError)
        check(if (fresh.partyState.isOwner) fresh.partyState.party?.permissions?.canInvite == true else fresh.partyState.canCreateParty)
        if (!fresh.partyState.isOwner) {
            check(!fresh.partyState.isEnrolled)
            partyActions.create(questId)
            fresh = loadDetail(fresh)
        }
        check(fresh.partyState.isOwner)
        val friends = partyActions.friends()
        fresh.copy(selectingFriends = true, friends = friends,
            partyState = fresh.partyState.copy(names = fresh.partyState.names + friends.mapNotNull { friend ->
                friend.name?.let { friend.userId to it }
            }.toMap()))
    }

    fun closeFriends() { if (!mutable.value.busy) mutable.update { it.copy(selectingFriends = false) } }

    fun inviteFriend(userId: Long) = perform {
        check(partyState.isOwner && partyState.party?.permissions?.canInvite == true)
        partyActions.invite(requireNotNull(partyState.party?.session?.id), userId)
        loadDetail(this)
    }

    fun acceptInvitation(memberId: Long) = perform {
        check(!partyState.isEnrolled && !partyState.hasActiveAssignment)
        check(partyState.invitations.any { it.memberId == memberId })
        partyActions.accept(memberId)
        loadDetail(this)
    }

    fun declineInvitation(memberId: Long) = perform {
        check(partyState.invitations.any { it.memberId == memberId })
        partyActions.decline(memberId)
        loadDetail(this)
    }

    fun leaveParty() = perform {
        check(partyState.party?.permissions?.canLeave == true)
        partyActions.leave(requireNotNull(partyState.party?.currentMember?.id))
        loadDetail(this).copy(stage = QuestStage.DETAIL)
    }

    fun removeMember(memberId: Long) = perform {
        check(partyState.isOwner && partyState.party?.permissions?.canRemoveMembers == true)
        partyActions.remove(memberId)
        loadDetail(this)
    }

    fun cancelParty() = perform {
        check(partyState.isOwner && partyState.party?.permissions?.canDeleteSession == true)
        partyActions.cancel(requireNotNull(partyState.party?.session?.id))
        loadDetail(this).copy(selectingFriends = false)
    }

    fun animationEnded() {
        if (mutable.value.stage == QuestStage.STARTING) mutable.update { it.copy(stage = QuestStage.ACTIVITIES) }
    }
    fun check(id: Long, checked: Boolean) = perform { copy(execution = service.check(requireNotNull(execution), id, checked)) }
    fun finish() = perform { copy(execution = service.finish(requireNotNull(execution)), stage = QuestStage.FINISHED) }

    private fun perform(refreshing: Boolean = false, action: suspend QuestExecutionState.() -> QuestExecutionState) {
        if (mutable.value.busy) return
        mutable.update { it.copy(busy = true, refreshing = refreshing, error = false) }
        viewModelScope.launch {
            try {
                mutable.value = action(mutable.value).copy(busy = false, refreshing = false, error = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // A response may have been lost after the server committed. Reconcile before another action.
                val current = mutable.value
                val reconciled = if (current.execution?.quest?.type == QuestType.COLLABORATIVE) {
                    try { loadDetail(current) }
                    catch (exception: CancellationException) { throw exception }
                    catch (_: Exception) { current.copy(partyState = current.partyState.copy(hasError = true)) }
                } else current
                mutable.value = reconciled.copy(busy = false, refreshing = false, error = true)
            }
        }
    }
}
