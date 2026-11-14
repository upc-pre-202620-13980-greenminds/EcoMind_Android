package pe.greenminds.ecomind.quests.infrastructure.remote

data class CreateFamilyPlanRequest(val familyId: Long, val items: List<FamilyPlanItemRequest>)
