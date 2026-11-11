package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.Friendship

interface FriendRepository {

    // Requests the user sent or received, in any state
    suspend fun getFriendships(userId: Long): Result<List<Friendship>>

    // The requester is always the user of the session
    suspend fun sendRequest(receiverId: Long): Result<Friendship>

    suspend fun respond(friendshipId: Long, accepted: Boolean): Result<Friendship>
}
