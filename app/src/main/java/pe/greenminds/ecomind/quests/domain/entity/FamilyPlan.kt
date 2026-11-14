package pe.greenminds.ecomind.quests.domain.entity

import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus

data class FamilyPlan(
    val id: Long,
    val familyId: Long,
    val ownerUserId: Long,
    val status: FamilyPlanStatus,
    val progress: Double,
    val items: List<FamilyPlanItem>
) {
    val completedItems: Int get() = items.count { it.progress >= 100.0 }
    val readyToComplete: Boolean get() = status == FamilyPlanStatus.ACTIVE &&
        progress >= 100.0 && items.isNotEmpty() && items.all { it.progress >= 100.0 }
}
