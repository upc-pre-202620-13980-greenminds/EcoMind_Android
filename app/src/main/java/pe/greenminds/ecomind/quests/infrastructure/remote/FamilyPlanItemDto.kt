package pe.greenminds.ecomind.quests.infrastructure.remote

data class FamilyPlanItemDto(
    val id: Long, val questId: Long, val collaborativeSessionId: Long?, val progress: Double
)
