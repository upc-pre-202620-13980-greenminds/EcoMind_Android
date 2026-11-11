package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.FriendRequest
import pe.greenminds.ecomind.users.domain.model.FriendRequests
import pe.greenminds.ecomind.users.domain.model.FriendshipStatus
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetFriendRequestsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val friendRepository: FriendRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(): Result<FriendRequests> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))
        val userId = session.accountId

        val friendships = friendRepository.getFriendships(userId).getOrElse { return Result.failure(it) }
        // One request for every profile instead of one request per friendship
        val profiles = profileRepository.getProfiles()
            .getOrElse { return Result.failure(it) }
            .associateBy { it.id }

        // A request is listed only if the profile of the other user exists
        val requests = friendships.mapNotNull { friendship ->
            profiles[friendship.otherUserId(userId)]?.let { profile ->
                friendship to FriendRequest(
                    friendshipId = friendship.id,
                    userId = profile.id,
                    name = profile.name,
                    socialRole = profile.socialRole,
                    status = friendship.status
                )
            }
        }

        val received = requests
            .filter { (friendship, _) ->
                friendship.receiverId == userId && friendship.status == FriendshipStatus.PENDING
            }
            .map { it.second }
        val sent = requests
            .filter { (friendship, _) ->
                friendship.requesterId == userId && friendship.status != FriendshipStatus.ACCEPTED
            }
            .map { it.second }

        return Result.success(FriendRequests(received = received, sent = sent))
    }
}
