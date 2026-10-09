package pe.greenminds.ecomind.quests.presentation.ui.progress

import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus

internal fun previewProgressEntry(id: Long, title: String, percent: Int) = ProgressEntry(
    questUser = QuestUser(
        id = id, userId = 1, questId = id,
        status = if (percent == 100) QuestStatus.COMPLETED else QuestStatus.IN_PROGRESS,
        progress = percent.toDouble(), endDate = null
    ),
    title = title
)
