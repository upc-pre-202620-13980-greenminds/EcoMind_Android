package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestStateDto(
    val session: CollabQuestSessionDto?,
    val members: List<CollabQuestMemberDto>,
    val currentMember: CollabQuestMemberDto?,
    val pendingInvitation: CollabQuestMemberDto?,
    val permissions: CollabQuestPermissionsDto,
    val counters: CollabQuestCountersDto,
    val unavailableUserIds: List<Long>,
    val source: String,
    val familyPlanId: Long?,
    val familyPlanItemId: Long?
)
