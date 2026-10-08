package pe.greenminds.ecomind.community.application

import pe.greenminds.ecomind.community.domain.model.CommunityEvent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import javax.inject.Inject

class GetCommunityEventsUseCase @Inject constructor(private val repository: CommunityRepository) {

    suspend operator fun invoke(): Result<List<CommunityEvent>> = repository.getEvents()
}
