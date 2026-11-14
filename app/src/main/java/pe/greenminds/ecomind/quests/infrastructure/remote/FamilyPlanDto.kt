package pe.greenminds.ecomind.quests.infrastructure.remote

data class FamilyPlanDto(
    val id: Long, val familyId: Long, val ownerUserId: Long,
    val status: String, val progress: Double, val items: List<FamilyPlanItemDto>
)
