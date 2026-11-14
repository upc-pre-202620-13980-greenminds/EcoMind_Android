package pe.greenminds.ecomind.quests.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE

data class CreatePartyRequest(val questId: Long)
data class InvitePartyMemberRequest(val sessionId: Long, val invitedUserId: Long)

interface CollaborativeQuestApi {
    @GET("collaborative-quest-members/me")
    suspend fun memberships(@Query("status") status: String): Response<List<CollabQuestMemberDto>>
    @POST("collaborative-quests")
    suspend fun createParty(@Body request: CreatePartyRequest): Response<CollabQuestSessionDto>

    @POST("collaborative-quests/{sessionId}/start")
    suspend fun startParty(@Path("sessionId") sessionId: Long): Response<CollabQuestSessionDto>

    @DELETE("collaborative-quests/{sessionId}")
    suspend fun cancelParty(@Path("sessionId") sessionId: Long): Response<Unit>

    @POST("collaborative-quest-members")
    suspend fun invite(@Body request: InvitePartyMemberRequest): Response<CollabQuestMemberDto>

    @PATCH("collaborative-quest-members/{memberId}/accept")
    suspend fun accept(@Path("memberId") memberId: Long): Response<CollabQuestMemberDto>

    @PATCH("collaborative-quest-members/{memberId}/decline")
    suspend fun decline(@Path("memberId") memberId: Long): Response<CollabQuestMemberDto>

    @PATCH("collaborative-quest-members/{memberId}/leave")
    suspend fun leave(@Path("memberId") memberId: Long): Response<CollabQuestMemberDto>

    @PATCH("collaborative-quest-members/{memberId}/remove")
    suspend fun remove(@Path("memberId") memberId: Long): Response<CollabQuestMemberDto>
    @GET("collaborative-quest-members/me/invitations")
    suspend fun getInvitations(): Response<List<CollabQuestInvitationDto>>

    @GET("collaborative-quests/state")
    suspend fun getStateByQuest( @Query("questId") questId: Long): Response<CollabQuestStateDto>

    @GET("collaborative-quests/{sessionId}/state")
    suspend fun getStateBySession(@Path("sessionId") sessionId: Long): Response<CollabQuestStateDto>
}
