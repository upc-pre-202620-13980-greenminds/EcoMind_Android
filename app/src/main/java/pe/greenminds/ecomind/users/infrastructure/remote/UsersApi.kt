package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Profile and friend routes of the web services (Users context)
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
}
