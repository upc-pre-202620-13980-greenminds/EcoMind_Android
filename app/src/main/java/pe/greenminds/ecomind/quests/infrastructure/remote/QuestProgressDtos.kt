package pe.greenminds.ecomind.quests.infrastructure.remote

import pe.greenminds.ecomind.quests.domain.model.FamilyPlan
import pe.greenminds.ecomind.quests.domain.model.FamilyPlanItem
import pe.greenminds.ecomind.quests.domain.model.FamilyPlanStatus
import pe.greenminds.ecomind.quests.domain.model.QuestStatus
import pe.greenminds.ecomind.quests.domain.model.QuestUser

// Same fields, names and types as the resources of the web services (Quests context)

// GET /api/v1/quest-users/user/{userId}/status/{status}
data class QuestUserDto(
    val id: Long,
    val userId: Long,
    val questId: Long,
    val status: String,
    // From 0 to 100
    val progress: Double,
    val endDate: String?,
    val collaborativeSessionId: Long?
)

// GET /api/v1/family-plans/active?familyId={id}
data class FamilyPlanDto(
    val id: Long,
    val familyId: Long,
    val ownerUserId: Long,
    val status: String,
    val progress: Double,
    val items: List<FamilyPlanItemDto>
)

data class FamilyPlanItemDto(
    val id: Long,
    val questId: Long,
    val collaborativeSessionId: Long?,
    val progress: Double
)

fun QuestUserDto.toDomain(): QuestUser {
    return QuestUser(
        id = id,
        questId = questId,
        status = QuestStatus.valueOf(status),
        progress = progress
    )
}

fun FamilyPlanItemDto.toDomain(): FamilyPlanItem {
    return FamilyPlanItem(
        id = id,
        questId = questId,
        progress = progress
    )
}

fun FamilyPlanDto.toDomain(): FamilyPlan {
    return FamilyPlan(
        id = id,
        familyId = familyId,
        status = FamilyPlanStatus.valueOf(status),
        progress = progress,
        items = items.map { it.toDomain() }
    )
}
