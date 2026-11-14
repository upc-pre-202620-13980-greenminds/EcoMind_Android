package pe.greenminds.ecomind.quests.domain.repositories

import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme

interface FamilyPlanRepository {
    suspend fun getPlans(familyId: Long): Result<List<FamilyPlan>>
    suspend fun getActivePlan(familyId: Long): Result<FamilyPlan?>
    suspend fun getPlan(planId: Long): Result<FamilyPlan>
    suspend fun createDraft(familyId: Long, questIds: List<Long>): Result<FamilyPlan>
    suspend fun updateDraft(planId: Long, questIds: List<Long>): Result<FamilyPlan>
    suspend fun activate(planId: Long): Result<FamilyPlan>
    suspend fun complete(planId: Long): Result<FamilyPlan>
    suspend fun delete(planId: Long): Result<FamilyPlan>
    suspend fun searchQuests(query: String, category: QuestCategory?, theme: QuestTheme?): Result<List<Quest>>
}
