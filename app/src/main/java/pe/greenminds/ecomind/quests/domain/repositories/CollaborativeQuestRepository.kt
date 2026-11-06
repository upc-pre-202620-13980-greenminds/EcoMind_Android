package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation
import pe.greenminds.ecomind.quests.domain.model.CollabQuestState
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestSession
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestMember

interface CollaborativeQuestRepository {
    suspend fun acceptedMemberships(): Result<List<CollabQuestMember>>
    suspend fun createParty(questId: Long): Result<CollabQuestSession>
    suspend fun startParty(sessionId: Long): Result<Unit>
    suspend fun cancelParty(sessionId: Long): Result<Unit>
    suspend fun invite(sessionId: Long, userId: Long): Result<Unit>
    suspend fun accept(memberId: Long): Result<Unit>
    suspend fun decline(memberId: Long): Result<Unit>
    suspend fun leave(memberId: Long): Result<Unit>
    suspend fun remove(memberId: Long): Result<Unit>
    suspend fun getInvitations(): Result<List<CollabQuestInvitation>>

    suspend fun getStateByQuest(questId: Long): Result<CollabQuestState>

    suspend fun getStateBySession(sessionId: Long): Result<CollabQuestState>
}
