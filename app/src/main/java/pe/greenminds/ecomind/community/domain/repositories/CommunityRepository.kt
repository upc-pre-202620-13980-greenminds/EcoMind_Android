package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.entity.CommunityEvent

interface CommunityRepository {

    suspend fun getEvents(): Result<List<CommunityEvent>>
}
