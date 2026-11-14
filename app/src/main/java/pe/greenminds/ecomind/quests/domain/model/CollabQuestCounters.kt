package pe.greenminds.ecomind.quests.domain.model

data class CollabQuestCounters(
    val acceptedInvites: Int,
    val pendingInvites: Int,
    val activeInvites: Int,
    val maxInvites: Int
)
