package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class CreateCommunityEventUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(event: NewCommunityEvent): Result<CommunityEvent> = repository.createEvent(event)
}