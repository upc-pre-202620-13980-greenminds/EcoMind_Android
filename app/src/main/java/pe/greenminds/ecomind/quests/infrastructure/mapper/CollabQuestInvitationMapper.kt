package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation
import pe.greenminds.ecomind.quests.infrastructure.remote.CollabQuestInvitationDto

fun CollabQuestInvitationDto.toDomain(): CollabQuestInvitation =
    CollabQuestInvitation(
        memberId = memberId,
        sessionId = sessionId,
        questId = questId,
        questTitle = questTitle,
        organizerUserId = organizerUserId,
        invitedUserId = invitedUserId
    )