package pe.greenminds.ecomind.quests.infrastructure.implementation

import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.CreateFamilyPlanRequest
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanApi
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanItemRequest
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import pe.greenminds.ecomind.quests.infrastructure.remote.UpdateFamilyPlanRequest
import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import javax.inject.Inject

class RemoteFamilyPlanRepository @Inject constructor(
    private val api: FamilyPlanApi,
    private val quests: QuestApi
) : FamilyPlanRepository {
    override suspend fun getPlans(familyId: Long) = apiResult {
        api.getPlans(familyId).requireBody().map { it.toDomain() }
    }

    override suspend fun getActivePlan(familyId: Long) = apiResult {
        val response = api.getActivePlan(familyId)
        if (response.code() == 404) null else response.requireBody().toDomain()
    }

    override suspend fun getPlan(planId: Long) = apiResult { api.getPlan(planId).requireBody().toDomain() }

    override suspend fun createDraft(familyId: Long, questIds: List<Long>) = apiResult {
        api.createDraft(CreateFamilyPlanRequest(familyId, questIds.distinct().map(::FamilyPlanItemRequest)))
            .requireBody().toDomain()
    }

    override suspend fun updateDraft(planId: Long, questIds: List<Long>) = apiResult {
        api.updateDraft(planId, UpdateFamilyPlanRequest(questIds.distinct().map(::FamilyPlanItemRequest)))
            .requireBody().toDomain()
    }

    override suspend fun activate(planId: Long) = apiResult { api.activate(planId).requireBody().toDomain() }
    override suspend fun complete(planId: Long) = apiResult { api.complete(planId).requireBody().toDomain() }
    override suspend fun delete(planId: Long) = apiResult { api.delete(planId).requireBody().toDomain() }

    override suspend fun searchQuests(query: String, category: QuestCategory?, theme: QuestTheme?) = apiResult {
        quests.searchQuests(query.trim(), category?.name, QuestType.FAMILY.name, theme?.name)
            .requireBody().map { it.toDomain() }.filter { it.type == QuestType.FAMILY }
    }
}
