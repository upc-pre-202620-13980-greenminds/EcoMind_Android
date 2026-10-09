package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.entity.QuestUser

interface QuestProgressRepository {
    suspend fun getQuestUsers(): Result<List<QuestUser>>
    suspend fun getActivityUsers(): Result<List<ActivityUser>>
    suspend fun getQuests(): Result<List<Quest>>
}
