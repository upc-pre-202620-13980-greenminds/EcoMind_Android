package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.model.CommunityEvent

interface CommunityRepository {

    suspend fun getEvents(): Result<List<CommunityEvent>>
}
