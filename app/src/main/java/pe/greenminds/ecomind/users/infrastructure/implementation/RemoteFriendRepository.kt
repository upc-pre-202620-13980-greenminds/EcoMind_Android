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
import javax.inject.Inject

class RemoteFriendRepository @Inject constructor(
    private val api: UsersApi
) : FriendRepository {

    override suspend fun getFriendships(userId: Long): Result<List<Friendship>> = apiResult {
        api.getFriendships(userId).requireBody().map { it.toDomain() }
    }

    override suspend fun sendRequest(receiverId: Long): Result<Friendship> {
        return apiResult {
            api.sendFriendRequest(SendFriendRequestRequest(receiverId)).requireBody().toDomain()
        }.toFriendRequestResult()
    }

    override suspend fun respond(friendshipId: Long, accepted: Boolean): Result<Friendship> {
        return apiResult {
            api.respondFriendRequest(friendshipId, RespondFriendRequestRequest(accepted))
                .requireBody()
                .toDomain()
        }.toFriendRequestResult()
    }

    // The codes of the web services stay here; the rest of the app only sees the domain error
    private fun Result<Friendship>.toFriendRequestResult(): Result<Friendship> {
        val exception = exceptionOrNull() ?: return this
        val error = when ((exception as? ApiException)?.code) {
            "FRIENDSHIP_CONFLICT" -> FriendRequestError.ALREADY_EXISTS
            "FRIEND_REQUEST_REJECTED_RECENTLY" -> FriendRequestError.REJECTED_RECENTLY
            "FRIEND_REQUEST_ALREADY_ANSWERED" -> FriendRequestError.ALREADY_ANSWERED
            else -> FriendRequestError.UNKNOWN
        }
        return Result.failure(FriendRequestException(error))
    }
}
