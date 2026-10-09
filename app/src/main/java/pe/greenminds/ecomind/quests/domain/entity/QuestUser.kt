package pe.greenminds.ecomind.quests.domain.entity

import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus

data class QuestUser (
    val id:Long,
    val userId: Long,
    val questId: Long,
    val status: QuestStatus,
    val progress: Double,
    val endDate: String?,
    val collaborativeSessionId: Long? = null
)