package pe.greenminds.ecomind.quests.infrastructure.remote

data class CreateQuestUserRequest(val questId: Long, val collaborativeSessionId: Long? = null)
data class QuestUserDto(val id: Long, val userId: Long, val questId: Long,
    val status: String, val progress: Double, val endDate: String?, val collaborativeSessionId: Long?)
