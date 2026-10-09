package pe.greenminds.ecomind.quests.domain.entity

import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestPublicationStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestReward
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import java.time.LocalDate

data class Quest(
    val id: Long,
    val versionGroupId: Long,
    val versionNumber: Int,
    val publicationStatus: QuestPublicationStatus,
    val minigameId: Long?,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val type: QuestType,
    val reward: QuestReward,
    val targetAge: Int?,
    val estimatedMinutes: Int?,
    val theme: QuestTheme,
    val assignedDate: LocalDate?,
    val imageUrl: String?
)