package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.entity.*

interface QuestExecutionRepository {
    suspend fun find(questId: Long): QuestUser?
    suspend fun start(questId: Long): QuestUser
    suspend fun get(id: Long): QuestUser
    suspend fun activities(questId: Long): List<Activity>
    suspend fun activityUsers(questUserId: Long): List<ActivityUser>
    suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser
    suspend fun finish(questUserId: Long): QuestUser
}
