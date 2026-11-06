package pe.greenminds.ecomind.quests.infrastructure.implementation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.domain.model.PartyFriend
import pe.greenminds.ecomind.quests.domain.repositories.QuestPartyPeopleRepository
import pe.greenminds.ecomind.users.infrastructure.remote.PartyPeopleApi
import javax.inject.Inject

class RemoteQuestPartyPeopleRepository @Inject constructor(
    private val api: PartyPeopleApi,
    private val sessions: SessionRepository
) : QuestPartyPeopleRepository {
    override suspend fun getFriends(): List<PartyFriend> {
        val userId = requireNotNull(sessions.getSession().first()).accountId
        val friendIds = api.friends(userId).filter {
            it.status == "ACCEPTED" && (it.requesterId == userId || it.receiverId == userId)
        }.map { if (it.requesterId == userId) it.receiverId else it.requesterId }.distinct()
        val names = getNames(friendIds.toSet())
        return friendIds.map { PartyFriend(it, names[it]) }.sortedBy { it.name.orEmpty().lowercase() }
    }

    override suspend fun getNames(userIds: Set<Long>): Map<Long, String> = coroutineScope {
        userIds.map { id ->
            async {
                try { id to api.profile(id).name }
                catch (exception: CancellationException) { throw exception }
                catch (_: Exception) { null }
            }
        }.awaitAll().filterNotNull().toMap()
    }
}
