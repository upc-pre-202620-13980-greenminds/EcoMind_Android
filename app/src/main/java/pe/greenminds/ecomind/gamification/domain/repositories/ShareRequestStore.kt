package pe.greenminds.ecomind.gamification.domain.repositories

data class ShareRequest(val requestId: String, val awardId: String, val communityId: Long)
interface ShareRequestStore {
    suspend fun read(user: Long, award: String): ShareRequest?
    suspend fun getOrCreate(user: Long, award: String, community: Long): ShareRequest
}
