package pe.greenminds.ecomind.users.infrastructure.implementation

import pe.greenminds.ecomind.shared.infrastructure.remote.ApiException
import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import pe.greenminds.ecomind.users.domain.model.FriendRequestError
import pe.greenminds.ecomind.users.domain.model.FriendRequestException
import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.infrastructure.remote.RespondFriendRequestRequest
import pe.greenminds.ecomind.users.infrastructure.remote.SendFriendRequestRequest
import pe.greenminds.ecomind.users.infrastructure.remote.UsersApi
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import retrofit2.HttpException
import javax.inject.Inject

class RemoteFriendRepository @Inject constructor(
    private val api: UsersApi
) : FriendRepository {

    override suspend fun getFriendships(userId: Long): Result<List<Friendship>> = apiResult {
        api.getFriendships(userId).requireBody().map { it.toDomain() }
    }

    override suspend fun sendRequest(receiverId: Long): Result<Friendship> {
        return apiResult {
            api.sendFriendRequest(SendFriendRequestRequest(receiverId = receiverId))
                .requireBody()
                .toDomain()
        }.toFriendRequestResult()
    }

    override suspend fun sendRequestWithCode(invitationCode: String): Result<Friendship> {
        return apiResult {
            api.sendFriendRequest(SendFriendRequestRequest(invitationCode = invitationCode))
                .requireBody()
                .toDomain()
        }.toFriendRequestResult()
    }

    override suspend fun respond(friendshipId: Long, accepted: Boolean): Result<Friendship> {
        return apiResult {
            api.respondFriendRequest(friendshipId, RespondFriendRequestRequest(accepted))
                .requireBody()
                .toDomain()
        }.toFriendRequestResult()
    }

    override suspend fun remove(friendshipId: Long): Result<Unit> {
        return apiResult {
            // The answer has no body, so only the status code is checked
            val response = api.removeFriendship(friendshipId)
            if (!response.isSuccessful) throw HttpException(response)
        }.toFriendRequestResult()
    }

    // The codes of the web services stay here; the rest of the app only sees the domain error
    private fun <T> Result<T>.toFriendRequestResult(): Result<T> {
        val exception = exceptionOrNull() ?: return this
        val error = when ((exception as? ApiException)?.code) {
            "FRIENDSHIP_CONFLICT" -> FriendRequestError.ALREADY_EXISTS
            "FRIEND_REQUEST_REJECTED_RECENTLY" -> FriendRequestError.REJECTED_RECENTLY
            "FRIEND_REQUEST_UNANSWERED_RECENTLY" -> FriendRequestError.UNANSWERED_RECENTLY
            "FRIEND_REQUEST_EXPIRED" -> FriendRequestError.EXPIRED
            "FRIEND_REQUEST_ALREADY_ANSWERED" -> FriendRequestError.ALREADY_ANSWERED
            "FRIEND_REQUEST_TO_SELF" -> FriendRequestError.SELF_REQUEST
            "INVITATION_CODE_NOT_FOUND" -> FriendRequestError.INVITATION_CODE_NOT_FOUND
            "FRIENDSHIP_ACCESS_FORBIDDEN" -> FriendRequestError.NOT_PART_OF_FRIENDSHIP
            "FRIENDSHIP_IN_WAITING_PERIOD" -> FriendRequestError.IN_WAITING_PERIOD
            else -> FriendRequestError.UNKNOWN
        }
        return Result.failure(FriendRequestException(error))
    }
}
