package pe.greenminds.ecomind.quests.infrastructure.remote

import retrofit2.Response
import retrofit2.http.*

data class ActivityDto(val id: Long, val questId: Long, val description: String, val order: Int,
    val type: String, val activityConfiguration: Map<String, Any>?, val image: String?)
data class ActivityUserDto(val id: Long, val questUserId: Long, val activityId: Long,
    val progress: Double, val endDate: String?, val activityDescription: String?,
    val activityConfiguration: Map<String, Any>?, val collaborativeSessionId: Long?)
data class CheckboxRequest(val data: Map<String, Boolean>)

interface QuestExecutionApi {
    @GET("quest-users/me/quest/{questId}") suspend fun find(@Path("questId") id: Long): Response<QuestUserDto>
    @POST("quest-users") suspend fun start(@Body request: CreateQuestUserRequest): QuestUserDto
    @GET("quest-users/{id}") suspend fun get(@Path("id") id: Long): QuestUserDto
    @GET("activities/quest/{id}") suspend fun activities(@Path("id") id: Long): List<ActivityDto>
    @GET("activity-users/quest-user/{id}") suspend fun activityUsers(@Path("id") id: Long): List<ActivityUserDto>
    @POST("activity-users/{id}/submit") suspend fun check(@Path("id") id: Long, @Body request: CheckboxRequest): ActivityUserDto
    @POST("quest-users/{id}/complete") suspend fun finish(@Path("id") id: Long): QuestUserDto
}
