package pe.greenminds.ecomind.community.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
}