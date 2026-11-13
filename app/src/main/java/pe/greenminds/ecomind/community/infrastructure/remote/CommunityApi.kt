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
import retrofit2.http.PATCH

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

    @GET("Community/Posts")
    suspend fun getPosts(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityPostDto>>

    @GET("Community/Achievements")
    suspend fun getAchievements(
        @Query("community_id") communityId: Long
    ): Response<List<CommunityAchievementDto>>

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

    @GET("Community/Events/Participation")
    suspend fun getEventParticipation(
        @Query("community_id") communityId: Long
    ): Response<List<EventParticipationDto>>

    @GET("Community/Events/Mine")
    suspend fun getMyEvents(): Response<List<EventParticipationDto>>

    @POST("Community/Events/{eventId}/Registrations")
    suspend fun registerForEvent(
        @Path("eventId") eventId: Long,
        @Body request: RegisterForEventRequest
    ): Response<Unit>

    @DELETE("Community/Events/{eventId}/Registrations/me")
    suspend fun leaveEvent(
        @Path("eventId") eventId: Long
    ): Response<Unit>

    @PATCH("Community/Events/{eventId}/Registrations/{registrationId}/cancel")
    suspend fun cancelEventGroup(
        @Path("eventId") eventId: Long,
        @Path("registrationId") registrationId: Long
    ): Response<Unit>

    @GET("Community/Events/{eventId}/Chat/Messages")
    suspend fun getEventChatMessages(
        @Path("eventId") eventId: Long,
        @Query("after_id") afterId: Long
    ): Response<EventChatPageDto>

    @POST("Community/Events/{eventId}/Chat/Messages")
    suspend fun sendEventChatMessage(
        @Path("eventId") eventId: Long,
        @Body request: SendEventChatMessageRequest
    ): Response<EventChatMessageDto>

    @DELETE("Community/Events/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: Long
    ): Response<Unit>

    @POST("Community/Communities/Topics")
    suspend fun createTopicCommunity(
        @Body request: CreateTopicCommunityRequest
    ): Response<CommunityDto>

    @GET("user/{id}")
    suspend fun getPostAuthor(
        @Path("id") accountId: Long
    ): Response<PostAuthorDto>

    @GET("Community/Posts/{postId}/Reactions")
    suspend fun getPostReactions(
        @Path("postId") postId: Long
    ): Response<List<PostReactionDto>>

    @POST("Community/Post-Reactions")
    suspend fun setPostReaction(
        @Body request: SetPostReactionRequest
    ): Response<PostReactionDto>

    @DELETE("Community/Posts/{postId}/Reactions")
    suspend fun removePostReaction(
        @Path("postId") postId: Long
    ): Response<Unit>

    @GET("Community/Community-goals/Panel")
    suspend fun getGoalPanel(
        @Query("community_id") communityId: Long
    ): Response<CommunityGoalPanelDto>

    @POST("Community/Community-goals")
    suspend fun createGoal(
        @Body request: CreateCommunityGoalRequest
    ): Response<CommunityGoalDto>

    @POST("Community/Community-goals/{id}/Participants")
    suspend fun joinGoal(
        @Path("id") goalId: Long
    ): Response<CommunityGoalDto>
}