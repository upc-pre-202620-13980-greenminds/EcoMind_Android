package pe.greenminds.ecomind.quests.infrastructure.implementation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade
import pe.greenminds.ecomind.quests.domain.model.PartyPerson
import pe.greenminds.ecomind.quests.domain.repositories.QuestPartyPeopleRepository
import pe.greenminds.ecomind.users.infrastructure.remote.PartyPeopleApi
import javax.inject.Inject

class RemoteQuestPartyPeopleRepository @Inject constructor(
    private val api: PartyPeopleApi,
    private val sessions: SessionRepository,
    private val monetization: MonetizationContextFacade
) : QuestPartyPeopleRepository {
    override suspend fun getFriends(): List<PartyPerson> {
        val userId = requireNotNull(sessions.getSession().first()).accountId
        val friendIds = api.friends(userId).filter {
            it.status == "ACCEPTED" && (it.requesterId == userId || it.receiverId == userId)
        }.map { if (it.requesterId == userId) it.receiverId else it.requesterId }.distinct()
        val profiles = getPeople(friendIds.toSet())
        return friendIds.map { profiles[it] ?: PartyPerson(it, name = null) }
            .sortedBy { it.name.orEmpty().lowercase() }
    }

    override suspend fun getPeople(userIds: Set<Long>): Map<Long, PartyPerson> = coroutineScope {
        val currentUserId = if (userIds.isEmpty()) null else sessions.getSession().first()?.accountId
        userIds.map { id ->
            async {
                val profile = try {
                    api.profile(id)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    null
                }
                val avatar = profile?.avatarImageReference?.takeIf { it.isNotBlank() }
                    ?: if (id == currentUserId) monetization.getEquippedCosmetics().avatar?.imageReference else null
                if (profile == null && avatar == null) null else id to PartyPerson(id, profile?.name, avatar)
            }
        }.awaitAll().filterNotNull().toMap()
    }
}
