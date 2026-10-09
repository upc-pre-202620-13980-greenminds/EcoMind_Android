package pe.greenminds.ecomind.quests.infrastructure.remote
import retrofit2.http.*
data class QuestAssignmentDto(val id: Long, val userId: Long, val questId: Long, val status: String, val progress: Double)
data class ActivityDto(val id: Long, val questId: Long, val description: String, val order: Int, val type: String)
data class ActivityAssignmentDto(val id: Long, val questUserId: Long, val activityId: Long, val progress: Double)
data class StartQuestBody(val questId: Long)
data class SubmitActivityBody(val data: Map<String, Boolean>)
interface QuestApi {
    @GET("quest-users/me/status/{status}") suspend fun byStatus(@Header("Authorization") token: String, @Path("status") status: String): List<QuestUserDto>
    @GET("family-plans/active") suspend fun familyPlan(@Header("Authorization") token: String, @Query("familyId") id: Long): FamilyPlanDto
    @GET("quests/{id}") suspend fun quest(@Header("Authorization") token: String, @Path("id") id: Long): QuestDto
    @GET("quests/search") suspend fun search(@Header("Authorization") token: String, @Query("title") title: String,
        @Query("category") category: String?, @Query("questType") type: String?): List<QuestDto>
    @GET("activities/quest/{id}") suspend fun activities(@Header("Authorization") token: String, @Path("id") id: Long): List<ActivityDto>
    @GET("quest-users/me/quest/{id}") suspend fun assignment(@Header("Authorization") token: String, @Path("id") id: Long): QuestAssignmentDto
    @POST("quest-users") suspend fun start(@Header("Authorization") token: String, @Body body: StartQuestBody): QuestAssignmentDto
    @GET("activity-users/quest-user/{id}") suspend fun steps(@Header("Authorization") token: String, @Path("id") id: Long): List<ActivityAssignmentDto>
    @POST("activity-users/{id}/submit") suspend fun submit(@Header("Authorization") token: String, @Path("id") id: Long, @Body body: SubmitActivityBody): ActivityAssignmentDto
    @POST("quest-users/{id}/complete") suspend fun finish(@Header("Authorization") token: String, @Path("id") id: Long): QuestAssignmentDto
}
