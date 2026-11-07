package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.entity.FamilyPlanItem
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanDto

fun FamilyPlanDto.toDomain() = FamilyPlan(
    id, familyId, ownerUserId, FamilyPlanStatus.valueOf(status), progress,
    items.map { FamilyPlanItem(it.id, it.questId, it.collaborativeSessionId, it.progress) }
)
