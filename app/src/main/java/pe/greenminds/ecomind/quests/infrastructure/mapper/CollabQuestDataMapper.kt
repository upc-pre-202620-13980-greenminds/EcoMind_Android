package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.model.CollabQuestCounters
import pe.greenminds.ecomind.quests.domain.model.CollabQuestPermissions
import pe.greenminds.ecomind.quests.domain.model.CollabQuestState
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestCountersDto
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestPermissionsDto
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestStateDto

fun CollabQuestStateDto.toDomain(): CollabQuestState = CollabQuestState(
    session = session?.toDomain(),
    members = members.map { it.toDomain() },
    currentMember = currentMember?.toDomain(),
    pendingInvitation = pendingInvitation?.toDomain(),
    permissions = permissions.toDomain(),
    counters = counters.toDomain(),
    unavailableUserIds = unavailableUserIds,
    source = source,
    familyPlanId = familyPlanId,
    familyPlanItemId = familyPlanItemId
)

fun CollabQuestPermissionsDto.toDomain(): CollabQuestPermissions = CollabQuestPermissions(
    canInvite = canInvite,
    canStart = canStart,
    canAcceptInvitation = canAcceptInvitation,
    canLeave = canLeave,
    canRemoveMembers = canRemoveMembers,
    canDeleteSession = canDeleteSession
)

fun CollabQuestCountersDto.toDomain(): CollabQuestCounters = CollabQuestCounters(
    acceptedInvites = acceptedInvites,
    pendingInvites = pendingInvites,
    activeInvites = activeInvites,
    maxInvites = maxInvites
)
