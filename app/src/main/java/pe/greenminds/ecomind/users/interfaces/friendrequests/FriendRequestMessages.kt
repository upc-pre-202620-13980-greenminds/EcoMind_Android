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
        else -> R.string.friend_request_failed
    }
}
