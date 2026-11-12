package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class GetEventChatMessagesUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(eventId: Long, afterId: Long) =
        repository.getEventChatMessages(eventId, afterId)
}