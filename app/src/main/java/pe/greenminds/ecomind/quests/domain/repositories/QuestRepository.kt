package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType

interface QuestRepository {

    suspend fun getQuests(): Result<List<Quest>>

    suspend fun searchQuests(
        query: String,
        category: QuestCategory? = null,
        questType: QuestType? = null
    ): Result<List<Quest>>

    suspend fun getQuest(questId: Long): Result<Quest>
}
