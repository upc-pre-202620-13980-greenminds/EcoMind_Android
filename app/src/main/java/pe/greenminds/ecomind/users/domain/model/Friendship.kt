package pe.greenminds.ecomind.users.domain.model

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

// Friend request between two users, as the web services store it
data class Friendship(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: FriendshipStatus
) {
    fun otherUserId(userId: Long): Long = if (requesterId == userId) receiverId else requesterId
}

// A friend with the data of their profile, ready to be listed
data class Friend(
    val userId: Long,
    val name: String,
    val socialRole: SocialRole
)

data class FriendsOverview(
    val friends: List<Friend>,
    // Requests the user received and has not answered
    val pendingRequests: Int
)
