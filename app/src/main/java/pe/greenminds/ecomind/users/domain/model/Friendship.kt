package pe.greenminds.ecomind.users.domain.model

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

// How another user is related to the current one; decides the action shown in the search
enum class FriendRelation {
    NONE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    FRIENDS
}

// Friend request between two users, as the web services store it
data class Friendship(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: FriendshipStatus
) {
    fun otherUserId(userId: Long): Long = if (requesterId == userId) receiverId else requesterId

    // A rejected request counts as no relation: the web services decide if it can be sent again
    fun relationFor(userId: Long): FriendRelation = when (status) {
        FriendshipStatus.ACCEPTED -> FriendRelation.FRIENDS
        FriendshipStatus.PENDING ->
            if (requesterId == userId) FriendRelation.REQUEST_SENT else FriendRelation.REQUEST_RECEIVED
        FriendshipStatus.REJECTED -> FriendRelation.NONE
    }
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

// A user found by the search, with the action that applies to them
data class UserSearchResult(
    val userId: Long,
    val name: String,
    val socialRole: SocialRole,
    val relation: FriendRelation
)

// A friend request with the name of the other user, ready to be listed
data class FriendRequest(
    val friendshipId: Long,
    val userId: Long,
    val name: String,
    val socialRole: SocialRole,
    val status: FriendshipStatus
)

data class FriendRequests(
    // Pending requests the user has to answer
    val received: List<FriendRequest>,
    // Requests the user sent that are pending or were declined
    val sent: List<FriendRequest>
)

// Reasons the web services give to refuse a friend request
enum class FriendRequestError {
    ALREADY_EXISTS,
    REJECTED_RECENTLY,
    ALREADY_ANSWERED,
    UNKNOWN
}

class FriendRequestException(val error: FriendRequestError) : Exception(error.name)
