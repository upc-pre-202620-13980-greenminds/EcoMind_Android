package pe.greenminds.ecomind.quests.infrastructure.remote

import pe.greenminds.ecomind.quests.domain.model.Quest
import pe.greenminds.ecomind.quests.domain.model.QuestCategory
import pe.greenminds.ecomind.quests.domain.model.QuestTheme
import pe.greenminds.ecomind.quests.domain.model.QuestType

// Same fields, names and types as QuestResource of the web services.
// GET /api/v1/quests, GET /api/v1/quests/{questId} and GET /api/v1/quests/search
data class QuestDto(
    val id: Long,
    val minigameId: Long?,
    val title: String,
    val description: String,
    val category: String,
    val type: String,
    val gemReward: Int,
    val ecopoints: Int,
    val age: Int,
    val time: Int,
    val theme: String,
    val assignedDate: String?,
    val image: String?
)

fun QuestDto.toDomain(): Quest {
    return Quest(
        id = id,
        title = title,
        description = description,
        category = QuestCategory.valueOf(category),
        type = QuestType.valueOf(type),
        theme = QuestTheme.valueOf(theme),
        gemReward = gemReward,
        ecopoints = ecopoints,
        minutes = time,
        recommendedAge = age,
        imageUrl = image,
        minigameId = minigameId
    )
}
