package pe.greenminds.ecomind.quests.domain.entity

data class FamilyPlanItem(
    val id: Long,
    val questId: Long,
    val collaborativeSessionId: Long?,
    val progress: Double
)
