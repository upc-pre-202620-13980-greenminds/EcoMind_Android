package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.InvitationDetails
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetInvitationStatusUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val friendRepository: FriendRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(friendshipId: Long): Result<InvitationDetails> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))
        val userId = session.accountId

        // There is no route for a single request, so it is looked for among the user's requests
        val friendship = friendRepository.getFriendships(userId)
            .getOrElse { return Result.failure(it) }
            .find { it.id == friendshipId }
            ?: return Result.failure(IllegalArgumentException("The invitation was not found"))
        val friend = profileRepository.getProfile(friendship.otherUserId(userId))
            .getOrElse { return Result.failure(it) }

        return Result.success(
            InvitationDetails(
                friendshipId = friendship.id,
                name = friend.name,
                socialRole = friend.socialRole,
                status = friendship.invitationStatus(System.currentTimeMillis())
            )
        )
    }
}
