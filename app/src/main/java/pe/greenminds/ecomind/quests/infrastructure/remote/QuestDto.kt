package pe.greenminds.ecomind.quests.infrastructure.remote

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestPublicationStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestReward
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import java.time.LocalDate

// Same fields, names and types as QuestResource of the web services.
// GET /api/v1/quests, GET /api/v1/quests/{questId} and GET /api/v1/quests/search
data class QuestDto(
    val id: Long,
    val versionGroupId: Long,
    val versionNumber: Int,
    val publicationStatus: String,
    val minigameId: Long?,
    val title: String,
    val description: String,
    val category: String,
    val type: String,
    val gemReward: Int,
    val ecopoints: Int,
    val age: Int?,
    val time: Int?,
    val theme: String,
    val assignedDate: String?,
    val image: String?
)

fun QuestDto.toDomain(): Quest {
    return Quest(
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
        imageUrl = image,
    )
}
