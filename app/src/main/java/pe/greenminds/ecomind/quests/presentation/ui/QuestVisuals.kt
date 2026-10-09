package pe.greenminds.ecomind.quests.presentation.ui

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType

val Quest.displayTheme: QuestTheme
    get() = when (type) {
        QuestType.COLLABORATIVE, QuestType.FAMILY -> QuestTheme.COLLABORATIVE
        QuestType.MINIGAME -> QuestTheme.MINIGAME
        else -> theme
    }
