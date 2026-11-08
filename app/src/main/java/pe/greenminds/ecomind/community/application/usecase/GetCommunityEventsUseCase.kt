package pe.greenminds.ecomind.community.application.usecase

import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import javax.inject.Inject

class GetCommunityEventsUseCase @Inject constructor(private val repository: CommunityRepository) {

    suspend operator fun invoke(): Result<List<CommunityEvent>> = repository.getEvents()
}
