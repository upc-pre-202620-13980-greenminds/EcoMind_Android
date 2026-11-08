package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FamilyMembershipApi {
    @GET("family_user")
    suspend fun getMemberships(@Query("user_id") userId: Long): Response<List<FamilyMemberDto>>
}
