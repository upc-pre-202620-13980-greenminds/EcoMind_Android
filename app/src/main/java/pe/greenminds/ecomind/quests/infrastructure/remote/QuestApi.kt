package pe.greenminds.ecomind.quests.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestApi {
    @GET("quests")
    suspend fun getQuests(): Response<List<QuestDto>>

    @GET("quests/search")
    suspend fun searchQuests(
        @Query("title") title: String,
        @Query("category") category: String?,
        @Query("questType") questType: String?
    ): Response<List<QuestDto>>

    @GET("quests/{questId}")
    suspend fun getQuest(@Path("questId") questId: Long): Response<QuestDto>
}
