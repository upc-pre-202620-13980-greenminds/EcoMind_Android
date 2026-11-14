package pe.greenminds.ecomind.users.interfaces.friendrequests

import androidx.annotation.StringRes
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.users.domain.model.FriendRequestError
import pe.greenminds.ecomind.users.domain.model.FriendRequestException

// Text shown when a friend request cannot be sent or answered; used by both friend screens
@StringRes
fun messageFor(exception: Throwable): Int {
    return when ((exception as? FriendRequestException)?.error) {
        FriendRequestError.REJECTED_RECENTLY -> R.string.friend_requests_retry_notice
        FriendRequestError.ALREADY_EXISTS -> R.string.friend_request_already_exists
        FriendRequestError.ALREADY_ANSWERED -> R.string.friend_request_already_answered
        FriendRequestError.UNANSWERED_RECENTLY -> R.string.friend_request_unanswered_recently
        FriendRequestError.EXPIRED -> R.string.friend_request_expired
        FriendRequestError.SELF_REQUEST -> R.string.invite_code_own
        FriendRequestError.INVITATION_CODE_NOT_FOUND -> R.string.invite_code_not_found
        FriendRequestError.INVALID_INVITATION_CODE -> R.string.invite_code_invalid
        FriendRequestError.NOT_PART_OF_FRIENDSHIP -> R.string.friend_profile_remove_forbidden
        FriendRequestError.IN_WAITING_PERIOD -> R.string.friend_profile_remove_waiting
        else -> R.string.friend_request_failed
    }
}
