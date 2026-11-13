package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.FriendProfile
import pe.greenminds.ecomind.users.domain.model.FriendshipStatus
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetFriendProfileUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(friendId: Long): Result<FriendProfile> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))
        val userId = session.accountId

        val profile = profileRepository.getProfile(friendId).getOrElse { return Result.failure(it) }
        // Only an accepted request between the two makes them friends
        val friendship = friendRepository.getFriendships(userId)
            .getOrElse { return Result.failure(it) }
            .find { it.otherUserId(userId) == friendId && it.status == FriendshipStatus.ACCEPTED }

        return Result.success(
            FriendProfile(
                userId = profile.id,
                name = profile.name,
                socialRole = profile.socialRole,
                streak = profile.streak,
                ecopoints = profile.ecopoints,
                friendshipId = friendship?.id
            )
        )
    }
}
