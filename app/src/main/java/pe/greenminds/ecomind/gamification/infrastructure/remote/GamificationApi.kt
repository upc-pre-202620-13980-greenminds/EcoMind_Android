package pe.greenminds.ecomind.gamification.infrastructure.remote

import retrofit2.http.*

data class ProgressDto(val userId: Long, val totalEcopoints: Long, val currentStreak: Int, val longestStreak: Int,
    val lastActivityDate: String?, val lastProtectedDate: String?)
data class RewardAmountDto(val ecopoints: Long, val gems: Int)
data class RewardSourceDto(val type: String, val executionId: String)
data class BeneficiaryDto(val type: String, val id: Long)
data class RewardHistoryDto(val id: String, val source: RewardSourceDto, val beneficiary: BeneficiaryDto,
    val baseReward: RewardAmountDto, val grantedReward: RewardAmountDto, val occurredAt: String)
data class ShareBody(val requestId: String, val awardId: String, val communityId: Long)
data class ShareDto(val requestId: String, val awardId: String, val requestedBy: Long, val communityId: Long,
    val status: String, val publicationId: Long?, val createdAt: String, val confirmedAt: String?)
data class FamilyMembershipDto(val id: Long, val familyId: Long, val userId: Long)
data class CommunityMembershipDto(val community_id: Long, val user_id: Long)
data class CommunityDto(val id: Long, val name: String)
data class FamilyScoreDto(val familyId: Long, val totalEcopoints: Long)
interface GamificationApi {
    @GET("gamification/me/progress") suspend fun progress(@Header("Authorization") token: String): ProgressDto
    @GET("gamification/rewards") suspend fun history(@Header("Authorization") token: String,
        @Query("from") from: String, @Query("to") to: String, @Query("page") page: Int,
        @Query("size") size: Int = 100): RankingPageDto<RewardHistoryDto>
    @GET("gamification/achievements") suspend fun catalog(@Header("Authorization") token: String,
        @Query("page") page: Int, @Query("size") size: Int = 100): List<AchievementDto>
    @GET("gamification/me/achievements") suspend fun awards(@Header("Authorization") token: String,
        @Query("page") page: Int, @Query("size") size: Int = 100): List<AchievementAwardDto>
    @GET("gamification/{scope}/{id}/achievements") suspend fun groupAwards(@Header("Authorization") token: String,
        @Path("scope") scope: String, @Path("id") id: Long, @Query("page") page: Int,
        @Query("size") size: Int = 100): List<AchievementAwardDto>
    @GET("gamification/rankings/{type}/participants") suspend fun participants(@Header("Authorization") token: String,
        @Path("type") type: String, @Query("page") page: Int, @Query("size") size: Int = 100): RankingPageDto<RankingEntryDto>
    @GET("gamification/rankings/{type}/transactions") suspend fun transactions(@Header("Authorization") token: String,
        @Path("type") type: String, @Query("from") from: String, @Query("to") to: String,
        @Query("page") page: Int, @Query("size") size: Int = 100): RankingPageDto<RankingTransactionDto>
    @GET("family_user") suspend fun familyMembership(@Header("Authorization") token: String,
        @Query("user_id") id: Long): List<FamilyMembershipDto>
    @GET("Community/Memberships") suspend fun memberships(@Header("Authorization") token: String): List<CommunityMembershipDto>
    @GET("Community/Communities") suspend fun communities(@Header("Authorization") token: String): List<CommunityDto>
    @GET("gamification/families/{id}/score") suspend fun familyScore(@Header("Authorization") token: String, @Path("id") id: Long): FamilyScoreDto
    @POST("gamification/achievement-shares") suspend fun share(@Header("Authorization") token: String, @Body body: ShareBody): ShareDto
    @GET("gamification/achievement-shares/{id}") suspend fun shareStatus(@Header("Authorization") token: String, @Path("id") id: String): ShareDto
}

// Arrays terminate on a short page; enveloped endpoints terminate only on hasNext.
internal suspend fun <T> collectArrayPages(fetch: suspend (Int) -> List<T>): List<T> {
    val items = mutableListOf<T>()
    var page = 0
    do { val batch = fetch(page++); items.addAll(batch); if (batch.size < 100) return items } while (page < 10000)
    error("The server did not finish pagination")
}
internal suspend fun <T> collectPages(fetch: suspend (Int) -> RankingPageDto<T>): List<T> {
    val items = mutableListOf<T>()
    var page = 0
    do {
        val batch = fetch(page)
        check(batch.page == page && (!batch.hasNext || batch.items.isNotEmpty())) { "Invalid server page" }
        items.addAll(batch.items); page++
        if (!batch.hasNext) return items
    } while (page < 10000)
    error("The server did not finish pagination")
}
