package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.FamilyPlanItem
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.entity.Activity
import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.valueobject.ActivityType
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus

data class FamilyQuestExecution(
    val details: FamilyPlanDetails,
    val item: FamilyPlanItem,
    val quest: Quest,
    val assignment: QuestUser,
    val activities: List<Activity>,
    val checks: List<ActivityUser>
) {
    val canCheck: Boolean get() = details.plan.status == FamilyPlanStatus.ACTIVE &&
        assignment.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
    val readyToReturnToPlan: Boolean get() = item.progress >= 100.0
    val unsupportedActivities: List<Activity> get() = activities.filter { it.type != ActivityType.CHECKBOX }
}
