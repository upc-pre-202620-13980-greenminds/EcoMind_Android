package pe.greenminds.ecomind.users.infrastructure.remote

import retrofit2.http.*
import pe.greenminds.ecomind.iam.domain.model.Session
interface UsersApi {
    @GET("user/{id}") suspend fun profile(@Tag session: Session, @Path("id") id: Long): UserProfileDto
    @GET("family") suspend fun families(@Tag session: Session): List<FamilyDto>
    @GET("family_user") suspend fun membership(@Tag session: Session, @Query("user_id") id: Long): List<FamilyMemberDto>
    @GET("friend") suspend fun friends(@Tag session: Session, @Query("user_id") id: Long): List<FriendDto>
}
