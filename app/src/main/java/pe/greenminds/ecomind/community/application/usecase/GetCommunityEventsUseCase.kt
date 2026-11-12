package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class GetCommunityEventsUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: Long): Result<List<CommunityEvent>> = repository.getEvents(communityId)
}