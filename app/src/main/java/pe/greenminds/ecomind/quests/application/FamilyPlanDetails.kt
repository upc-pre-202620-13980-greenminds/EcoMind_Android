package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.model.FamilyRole

data class FamilyPlanDetails(
    val plan: FamilyPlan,
    val membership: FamilyMembership,
    val quests: Map<Long, Quest>
) {
    val canManage: Boolean get() = membership.familyRole == FamilyRole.PARENT &&
        membership.userId == plan.ownerUserId
    val canEdit: Boolean get() = canManage && plan.status == FamilyPlanStatus.DRAFT
    val canActivate: Boolean get() = canEdit && plan.items.isNotEmpty()
    val canComplete: Boolean get() = canManage && plan.readyToComplete
    val canDelete: Boolean get() = canManage && plan.status in listOf(FamilyPlanStatus.DRAFT, FamilyPlanStatus.ACTIVE)
}
