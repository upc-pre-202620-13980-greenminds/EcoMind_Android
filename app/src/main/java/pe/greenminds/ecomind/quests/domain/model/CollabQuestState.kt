package pe.greenminds.ecomind.quests.domain.model

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestMember
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestSession

data class CollabQuestState(
    val session: CollabQuestSession?,
    val members: List<CollabQuestMember>,
    val currentMember: CollabQuestMember?,
    val pendingInvitation: CollabQuestMember?,
    val permissions: CollabQuestPermissions,
    val counters: CollabQuestCounters,
    val unavailableUserIds: List<Long>,
    val source: String,
    val familyPlanId: Long?,
    val familyPlanItemId: Long?
)
