package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

// The web services take the requester from the token, so only the receiver is needed
class SendFriendRequestUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(receiverId: Long): Result<Friendship> {
        return friendRepository.sendRequest(receiverId)
    }
}
