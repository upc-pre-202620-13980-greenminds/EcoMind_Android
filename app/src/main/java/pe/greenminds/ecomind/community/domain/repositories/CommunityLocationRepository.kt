package pe.greenminds.ecomind.community.domain.repositories

interface CommunityLocationRepository {
    suspend fun getCurrentLocality(): Result<String>
}