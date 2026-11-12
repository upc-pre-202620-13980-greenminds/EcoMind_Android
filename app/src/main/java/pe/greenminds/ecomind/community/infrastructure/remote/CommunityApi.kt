package pe.greenminds.ecomind.community.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
interface CommunityApi {

    @GET("Community/Memberships")
    suspend fun getMyMemberships(): Response<List<CommunityMembershipDto>>

    @GET("Community/Communities")
    suspend fun getCommunities(
        @Query("type") type: String,
        @Query("locality") locality: String?
    ): Response<List<CommunityDto>>

    @POST("Community/Communities/{communityId}/Memberships")
    suspend fun joinCommunity(
        @Path("communityId") communityId: Long
    ): Response<CommunityMembershipDto>

    @DELETE("Community/Communities/{communityId}/Memberships")
    suspend fun leaveCommunity(
        @Path("communityId") communityId: Long
    ): Response<Unit>

    @GET("Community/Events")
    suspend fun getEvents(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityEventDto>>

    @GET("Community/Posts")
    suspend fun getPosts(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityPostDto>>

    @GET("Community/Achievements")
    suspend fun getAchievements(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityAchievementDto>>

    @GET("Community/Community-goals")
    suspend fun getCommunityGoals(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityGoalDto>>

    @GET("user/{id}")
    suspend fun getEventCreatorProfile(
        @Path("id") accountId: Long
    ): Response<EventCreatorProfileDto>

    @POST("Community/Events")
    suspend fun createEvent(
        @Body request: CreateCommunityEventRequest
    ): Response<CommunityEventDto>

    @Multipart
    @POST("Community/Events/Images")
    suspend fun uploadEventImage(
        @Part file: MultipartBody.Part
    ): Response<EventImageDto>
}