package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PartyPeopleApi {
    @GET("friend")
    suspend fun friends(@Query("user_id") userId: Long): List<FriendDto>

    @GET("user/{id}")
    suspend fun profile(@Path("id") userId: Long): UserProfileDto
}
