package pe.greenminds.ecomind.gamification.domain.model

enum class RankingType {
    LOCAL,
    GLOBAL,
    FRIENDS,
    FAMILIES
}

enum class RankingPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

// A user or a family that takes part in a ranking, with the ecopoints of their whole history
data class RankingParticipant(
    val id: Long,
    val displayName: String,
    val totalEcopoints: Int
)

// Ecopoints one participant earned at a moment in time
data class RankingTransaction(
    val beneficiaryId: Long,
    val ecopoints: Int,
    val occurredAtMillis: Long
)

data class RankingEntry(
    val position: Int,
    val participantId: Long,
    val displayName: String,
    val ecopoints: Int,
    val isCurrentUser: Boolean
)

data class Ranking(
    // The first places, already ordered
    val entries: List<RankingEntry>,
    // Place of the user or their family; null when they have no ecopoints in the period
    val currentEntry: RankingEntry?
)
