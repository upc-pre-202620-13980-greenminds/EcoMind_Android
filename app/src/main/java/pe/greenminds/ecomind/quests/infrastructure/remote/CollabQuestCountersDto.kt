package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestCountersDto(
    val acceptedInvites: Int,
    val pendingInvites: Int,
    val activeInvites: Int,
    val maxInvites: Int
)
