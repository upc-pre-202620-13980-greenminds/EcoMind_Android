package pe.greenminds.ecomind.quests.infrastructure.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MinigameAttemptApi {
    @GET("minigames/{minigameId}")
    suspend fun getMinigame(@Path("minigameId") minigameId: Long): MinigameDto

    @GET("minigame-attempts")
    suspend fun getAttempts(@Query("minigameId") minigameId: Long): List<MinigameAttemptDto>

    @POST("minigame-attempts")
    suspend fun start(@Body request: StartMinigameAttemptRequest): MinigameAttemptDto

    @POST("minigame-attempts/{attemptId}/finish")
    suspend fun finish(
        @Path("attemptId") attemptId: Long,
        @Body request: FinishMinigameAttemptRequest
    ): MinigameAttemptDto

    @POST("minigame-attempts/{attemptId}/cancel")
    suspend fun cancel(@Path("attemptId") attemptId: Long): MinigameAttemptDto
}

data class MinigameDto(val url: String, val completionRules: MinigameCompletionRulesDto)
data class MinigameCompletionRulesDto(val minScore: Int?)
data class StartMinigameAttemptRequest(val questId: Long)
data class FinishMinigameAttemptRequest(val score: Int)

enum class MinigameAttemptStatus { STARTED, COMPLETED, CANCELLED }

data class MinigameAttemptDto(
    val id: Long,
    val questId: Long,
    val status: MinigameAttemptStatus,
    val score: Int?,
    val endDate: String?,
    val successful: Boolean?
)
