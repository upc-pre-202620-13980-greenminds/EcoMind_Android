package pe.greenminds.ecomind.quests.application.service

import pe.greenminds.ecomind.quests.domain.repositories.CollaborativeQuestRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestPartyPeopleRepository
import javax.inject.Inject
import pe.greenminds.ecomind.quests.domain.model.CollabQuestState
import kotlinx.coroutines.CancellationException

class QuestPartyService @Inject constructor(
    private val repository: CollaborativeQuestRepository,
    private val people: QuestPartyPeopleRepository
) {
    // The quest lookup may select a pending invitation before an accepted membership.
    suspend fun resolveEnrolledState(questId: Long, fallback: CollabQuestState): CollabQuestState {
        if (fallback.currentMember?.status == "ACCEPTED") return fallback
        for (member in repository.acceptedMemberships().getOrThrow()) {
            val result = repository.getStateBySession(member.sessionId)
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            val state = result.getOrNull() ?: continue
            if (state.session?.questId == questId && state.session.status in listOf("PENDING", "STARTED")) return state
        }
        return fallback
    }
    suspend fun create(questId: Long) = repository.createParty(questId).getOrThrow()
    suspend fun start(sessionId: Long) = repository.startParty(sessionId).getOrThrow()
    suspend fun cancel(sessionId: Long) = repository.cancelParty(sessionId).getOrThrow()
    suspend fun invite(sessionId: Long, userId: Long) = repository.invite(sessionId, userId).getOrThrow()
    suspend fun accept(memberId: Long) = repository.accept(memberId).getOrThrow()
    suspend fun decline(memberId: Long) = repository.decline(memberId).getOrThrow()
    suspend fun leave(memberId: Long) = repository.leave(memberId).getOrThrow()
    suspend fun remove(memberId: Long) = repository.remove(memberId).getOrThrow()
    suspend fun friends() = people.getFriends()
    suspend fun profiles(ids: Set<Long>) = people.getPeople(ids)
}
