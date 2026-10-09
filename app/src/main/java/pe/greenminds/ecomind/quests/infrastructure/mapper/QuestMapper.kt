package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestDto
import java.time.LocalDate

fun QuestDto.toDomain(): Quest = Quest(
    id = id,
    versionGroupId = versionGroupId,
    versionNumber = versionNumber,
    publicationStatus = QuestPublicationStatus.valueOf(publicationStatus),
    minigameId = minigameId,
    title = title,
    description = description,
    category = QuestCategory.valueOf(category),
    type = QuestType.valueOf(type),
    reward = QuestReward(gems = gemReward, ecopoints = ecopoints),
    targetAge = age,
    estimatedMinutes = time,
    theme = QuestTheme.valueOf(theme),
    assignedDate = assignedDate?.let(LocalDate::parse),
    imageUrl = image
)
