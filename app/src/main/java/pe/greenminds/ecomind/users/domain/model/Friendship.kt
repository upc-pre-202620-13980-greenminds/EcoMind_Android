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
    val status: FriendshipStatus,
    // When the request was sent, in milliseconds; null in the requests of the demo data
    val sentAtMillis: Long? = null
) {
    companion object {
        // Same rule as the web services (HU-039): an unanswered request expires after 48 hours
        const val RESPONSE_DEADLINE_MILLIS = 48L * 60 * 60 * 1000
    }

    fun otherUserId(userId: Long): Long = if (requesterId == userId) receiverId else requesterId

    fun isExpired(nowMillis: Long): Boolean {
        val sentAt = sentAtMillis ?: return false
        return status == FriendshipStatus.PENDING && nowMillis >= sentAt + RESPONSE_DEADLINE_MILLIS
    }

    // A rejected or expired request counts as no relation: the web services decide
    // if a new one can already be sent
    fun relationFor(userId: Long, nowMillis: Long): FriendRelation = when {
        status == FriendshipStatus.ACCEPTED -> FriendRelation.FRIENDS
        status == FriendshipStatus.REJECTED || isExpired(nowMillis) -> FriendRelation.NONE
        requesterId == userId -> FriendRelation.REQUEST_SENT
        else -> FriendRelation.REQUEST_RECEIVED
    }

    // State of a request the user sent, shown in the invitation status screen (frames 40 to 42)
    fun invitationStatus(nowMillis: Long): InvitationStatus = when {
        status == FriendshipStatus.ACCEPTED -> InvitationStatus.JOINED
        status == FriendshipStatus.REJECTED -> InvitationStatus.DECLINED
        isExpired(nowMillis) -> InvitationStatus.NO_RESPONSE
        else -> InvitationStatus.AWAITING
    }
}

enum class InvitationStatus {
    AWAITING,
    NO_RESPONSE,
    JOINED,
    DECLINED
}

// An invitation the user sent, with the name of the friend
data class InvitationDetails(
    val friendshipId: Long,
    val name: String,
    val socialRole: SocialRole,
    val status: InvitationStatus
)

// Result of inviting several contacts at once: some requests may fail while others are sent
data class InvitationResult(
    val sent: List<Friendship>,
    val failedUserIds: List<Long>,
    val firstError: Throwable? = null
)

// A friend with the data of their profile, ready to be listed
data class Friend(
    val userId: Long,
    val name: String,
    val socialRole: SocialRole,
    val avatarImageReference: String? = null
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
    UNANSWERED_RECENTLY,
    EXPIRED,
    ALREADY_ANSWERED,
    SELF_REQUEST,
    INVITATION_CODE_NOT_FOUND,
    INVALID_INVITATION_CODE,
    NOT_PART_OF_FRIENDSHIP,
    IN_WAITING_PERIOD,
    UNKNOWN
}

// Profile of another user as a friend sees it (frame 13): no gems and no edit options
data class FriendProfile(
    val userId: Long,
    val name: String,
    val socialRole: SocialRole,
    val streak: Int,
    val ecopoints: Int,
    // Id of the accepted friendship, needed to remove it; null when they are not friends
    val friendshipId: Long?
)

class FriendRequestException(val error: FriendRequestError) : Exception(error.name)
