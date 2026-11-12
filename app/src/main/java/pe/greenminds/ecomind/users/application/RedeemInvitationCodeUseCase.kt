package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.FriendRequestError
import pe.greenminds.ecomind.users.domain.model.FriendRequestException
import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.model.InvitationCode
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

// Sends a friend request to the owner of a code a friend shared
class RedeemInvitationCodeUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {

    suspend operator fun invoke(input: String): Result<Friendship> {
        val code = InvitationCode.normalize(input)
        if (!InvitationCode.isValid(code)) {
            return Result.failure(FriendRequestException(FriendRequestError.INVALID_INVITATION_CODE))
        }
        return friendRepository.sendRequestWithCode(code)
    }
}
