package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.model.PartyPerson

interface QuestPartyPeopleRepository {
    suspend fun getFriends(): List<PartyPerson>
    suspend fun getPeople(userIds: Set<Long>): Map<Long, PartyPerson>
}
