package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

// The web services check that the user of the token is one of the two friends
class RemoveFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(friendshipId: Long): Result<Unit> {
        return friendRepository.remove(friendshipId)
    }
}
