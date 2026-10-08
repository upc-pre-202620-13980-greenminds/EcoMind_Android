package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.Friendship

interface FriendRepository {

    // Requests the user sent or received, in any state
    suspend fun getFriendships(userId: Long): Result<List<Friendship>>
}
