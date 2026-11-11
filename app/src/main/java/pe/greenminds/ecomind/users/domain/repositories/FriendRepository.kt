package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.Friendship

interface FriendRepository {

    // Requests the user sent or received, in any state
    suspend fun getFriendships(userId: Long): Result<List<Friendship>>

    // The requester is always the user of the session
    suspend fun sendRequest(receiverId: Long): Result<Friendship>

    // The web services find the owner of the code and send the request to them
    suspend fun sendRequestWithCode(invitationCode: String): Result<Friendship>

    suspend fun respond(friendshipId: Long, accepted: Boolean): Result<Friendship>

    suspend fun remove(friendshipId: Long): Result<Unit>
}
