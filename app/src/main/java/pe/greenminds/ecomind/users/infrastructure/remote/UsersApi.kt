package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

// Profile routes of the web services (Users context)
interface UsersApi {

    @GET("user/{id}")
    suspend fun getProfile(@Path("id") userId: Long): Response<UserProfileDto>

    // Replaces name, streak, last streak date, ecopoints and gem balance at once
    @PUT("user/{id}")
    suspend fun updateProfile(
        @Path("id") userId: Long,
        @Body request: UpdateUserProfileRequest
    ): Response<UserProfileDto>
}
