package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.Friend
import pe.greenminds.ecomind.users.domain.model.FriendsOverview
import pe.greenminds.ecomind.users.domain.model.FriendshipStatus
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    private val friendRepository: FriendRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(userId: Long): Result<FriendsOverview> {
        val friendships = friendRepository.getFriendships(userId)
            .getOrElse { return Result.failure(it) }

        // A friendship only has ids, so the name and role come from the profile of each friend
        val friends = friendships
            .filter { it.status == FriendshipStatus.ACCEPTED }
            .mapNotNull { friendship ->
                val friendId = friendship.otherUserId(userId)
                profileRepository.getProfile(friendId).getOrNull()?.let { profile ->
                    Friend(
                        userId = profile.id,
                        name = profile.name,
                        socialRole = profile.socialRole
                    )
                }
            }

        val pendingRequests = friendships.count { friendship ->
            friendship.status == FriendshipStatus.PENDING && friendship.receiverId == userId
        }

        return Result.success(FriendsOverview(friends, pendingRequests))
    }
}
