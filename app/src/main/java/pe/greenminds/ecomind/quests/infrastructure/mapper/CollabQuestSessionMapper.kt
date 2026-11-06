package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestSession
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestSessionDto

fun CollabQuestSessionDto.toDomain(): CollabQuestSession =
    CollabQuestSession (
        id = id,
        questId = questId,
        ownerUserId = ownerUserId,
        status = status,
        createdAt = createdAt,
        startedAt  = startedAt,
        completedAt = completedAt
    )