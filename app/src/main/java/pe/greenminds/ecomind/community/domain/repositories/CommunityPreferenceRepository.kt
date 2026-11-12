package pe.greenminds.ecomind.community.domain.repositories

interface CommunityPreferenceRepository {
    suspend fun getPreferredLocality(): String?
    suspend fun savePreferredLocality(locality: String)
}