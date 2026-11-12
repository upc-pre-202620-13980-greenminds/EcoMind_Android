package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class CancelEventGroupUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(eventId: Long, registrationId: Long) =
        repository.cancelEventGroup(eventId, registrationId)
}