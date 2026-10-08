package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.model.FamilyPlan
import pe.greenminds.ecomind.quests.domain.model.QuestStatus
import pe.greenminds.ecomind.quests.domain.model.QuestUser

interface QuestProgressRepository {

    suspend fun getQuestUsers(userId: Long, status: QuestStatus): Result<List<QuestUser>>

    // Active plan of the family of the user; null when there is none.
    // The web services ask for the id of the family, which the implementation gets from Users.
    suspend fun getActiveFamilyPlan(userId: Long): Result<FamilyPlan?>
}
