package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.model.InvitationResult
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

// The web services take one receiver per request, so one request is sent for each contact
class SendInvitationsUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(receiverIds: List<Long>): InvitationResult {
        val sent = mutableListOf<Friendship>()
        val failed = mutableListOf<Long>()
        var firstError: Throwable? = null

        // A failed contact does not stop the others
        receiverIds.forEach { receiverId ->
            friendRepository.sendRequest(receiverId)
                .onSuccess { sent.add(it) }
                .onFailure { error ->
                    failed.add(receiverId)
                    if (firstError == null) firstError = error
                }
        }
        return InvitationResult(sent = sent, failedUserIds = failed, firstError = firstError)
    }
}
