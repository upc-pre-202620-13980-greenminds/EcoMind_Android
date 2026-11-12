package pe.greenminds.ecomind.community.infrastructure.mapper

import pe.greenminds.ecomind.community.domain.entity.EventParticipation
import pe.greenminds.ecomind.community.infrastructure.remote.EventParticipationDto

fun EventParticipationDto.toDomain() = EventParticipation(
    event = event.toDomain(),
    communityName = communityName,
    creatorName = creatorName,
    participantCount = participantCount,
    totalCapacity = totalCapacity,
    registrationId = registrationId,
    joined = joined,
    canCancelGroup = canCancelGroup,
    creator = creator,
    chatExpiresAt = chatExpiresAt,
    chatAvailable = chatAvailable,
    familyId = familyId
)