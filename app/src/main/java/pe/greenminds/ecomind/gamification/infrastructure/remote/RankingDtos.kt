package pe.greenminds.ecomind.gamification.infrastructure.remote

// Contracts reviewed against backend develop 19b85df and its Gamification REST resources.

// Page returned by both ranking routes
data class RankingPageDto<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)

// GET /api/v1/gamification/rankings/{type}/participants?page=&size=
data class RankingEntryDto(
    val beneficiaryId: Long,
    val displayName: String,
    val totalEcopoints: Long
)

// GET /api/v1/gamification/rankings/{type}/transactions?from=&to=&page=&size=
data class RankingTransactionDto(
    // UUID
    val id: String,
    val beneficiaryId: Long,
    val ecopoints: Long,
    // Instant in UTC, for example 2026-10-07T16:00:00Z
    val occurredAt: String
)
