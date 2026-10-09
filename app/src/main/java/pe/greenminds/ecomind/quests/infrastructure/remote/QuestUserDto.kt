package pe.greenminds.ecomind.quests.infrastructure.remote

import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus

data class CreateQuestUserRequest(
    val questId: Long,
    val collaborativeSessionId: Long? = null
)

data class QuestUserDto(
    val id:Long,
    val userId: Long,
    val questId: Long,
    val status: String,
    val progress: Double,
    val endDate: String?,
    val collaborativeSessionId: Long?
)

fun QuestUserDto.toDomain(): QuestUser {
    return QuestUser(
        id = id,
        userId = userId,
        questId = questId,
        status = QuestStatus.valueOf(status),
        progress = progress,
        endDate = endDate,
        collaborativeSessionId = collaborativeSessionId

    )
}