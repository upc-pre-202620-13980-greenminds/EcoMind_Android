package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.model.FamilyRole

data class FamilyPlanOverview(
    val membership: FamilyMembership?,
    val plans: List<FamilyPlan>
) {
    val isParent: Boolean get() = membership?.familyRole == FamilyRole.PARENT
    val activePlan: FamilyPlan? get() = plans.filter { it.status == FamilyPlanStatus.ACTIVE }.maxByOrNull { it.id }
    val draftPlan: FamilyPlan? get() = plans.filter {
        it.status == FamilyPlanStatus.DRAFT && it.ownerUserId == membership?.userId
    }.maxByOrNull { it.id }
}
