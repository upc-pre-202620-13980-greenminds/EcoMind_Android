package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

// The web services check that the user of the token is the one who received the request
class RespondFriendRequestUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(friendshipId: Long, accepted: Boolean): Result<Friendship> {
        return friendRepository.respond(friendshipId, accepted)
    }
}
