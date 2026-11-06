package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.model.PartyFriend

interface QuestPartyPeopleRepository {
    suspend fun getFriends(): List<PartyFriend>
    suspend fun getNames(userIds: Set<Long>): Map<Long, String>
}
