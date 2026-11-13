package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Profile, friend and family routes of the web services (Users context)
interface UsersApi {

    @GET("user")
    suspend fun getProfiles(): Response<List<UserProfileDto>>

    @GET("user/{id}")
    suspend fun getProfile(@Path("id") userId: Long): Response<UserProfileDto>

    // Replaces name, streak, last streak date, ecopoints and gem balance at once
    @PUT("user/{id}")
    suspend fun updateProfile(
        @Path("id") userId: Long,
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileDto>

    // Requests the user sent or received, in any state
    @GET("friend")
    suspend fun getFriendships(@Query("user_id") userId: Long): Response<List<FriendDto>>

    // The requester is the user of the token, so only the receiver is sent
    @POST("friend")
    suspend fun sendFriendRequest(@Body request: SendFriendRequestRequest): Response<FriendDto>

    // Only the user who received the request can answer it
    @PUT("friend/{id}")
    suspend fun respondFriendRequest(
        @Path("id") friendshipId: Long,
        @Body request: RespondFriendRequestRequest
    ): Response<FriendDto>

    // Membership of one user: an empty list when they have no family
    @GET("family_user")
    suspend fun getFamilyMemberships(@Query("user_id") userId: Long): Response<List<FamilyMemberDto>>

    // Every family with its members; there is no route for a single family
    @GET("family")
    suspend fun getFamilies(): Response<List<FamilyDto>>

    // Answers 204 without a body; either of the two users can remove the friendship
    @DELETE("friend/{id}")
    suspend fun removeFriendship(@Path("id") friendshipId: Long): Response<Unit>
}
