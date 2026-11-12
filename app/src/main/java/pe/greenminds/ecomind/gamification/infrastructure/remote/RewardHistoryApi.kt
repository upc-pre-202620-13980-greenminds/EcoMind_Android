package pe.greenminds.ecomind.gamification.infrastructure.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RewardHistoryApi {
    @GET("gamification/rewards")
    suspend fun history(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("page") page: Int
    ): RewardHistoryPageDto
}

data class RewardHistoryPageDto(val items: List<RewardHistoryEntryDto>, val hasNext: Boolean)

data class RewardHistoryEntryDto(
    val source: RewardSourceDto,
    val grantedReward: RewardAmountDto
)

data class RewardSourceDto(val type: String, val executionId: String)
data class RewardAmountDto(val ecopoints: Long, val gems: Int)
