package pe.greenminds.ecomind.quests.application.usecase

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation
import pe.greenminds.ecomind.quests.domain.repositories.CollaborativeQuestRepository
import javax.inject.Inject

class GetCollabQuestInvitationUseCase @Inject constructor(
    private val repository: CollaborativeQuestRepository
) {
    suspend operator fun invoke(): Result<List<CollabQuestInvitation>> = repository.getInvitations()
}