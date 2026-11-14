package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestMember
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestMemberDto

fun CollabQuestMemberDto.toDomain(): CollabQuestMember =
    CollabQuestMember(
        id = id,
        sessionId = sessionId,
        userId = userId,
        ownerId = ownerId,
        role = role,
        status = status,
        answerDate = answerDate,
        revokeDate = revokeDate
    )