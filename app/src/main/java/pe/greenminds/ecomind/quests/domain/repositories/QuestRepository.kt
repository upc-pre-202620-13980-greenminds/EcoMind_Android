package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.model.Quest
import pe.greenminds.ecomind.quests.domain.model.QuestFilter

interface QuestRepository {

    suspend fun searchQuests(filter: QuestFilter): Result<List<Quest>>

    suspend fun getQuest(questId: Long): Result<Quest>
}
