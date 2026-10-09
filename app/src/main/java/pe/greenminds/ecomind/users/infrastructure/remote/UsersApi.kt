package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.http.*
interface UsersApi {
    @GET("user/{id}") suspend fun profile(@Header("Authorization") token: String, @Path("id") id: Long): UserProfileDto
    @GET("family") suspend fun families(@Header("Authorization") token: String): List<FamilyDto>
    @GET("family_user") suspend fun membership(@Header("Authorization") token: String, @Query("user_id") id: Long): List<FamilyMemberDto>
    @GET("friend") suspend fun friends(@Header("Authorization") token: String, @Query("user_id") id: Long): List<FriendDto>
}
