package pe.greenminds.ecomind.users.interfaces.friendrequests

import androidx.annotation.StringRes
import pe.greenminds.ecomind.users.domain.model.FriendRequests

data class FriendRequestsUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val requests: FriendRequests? = null,
    // Request being accepted or declined, so its buttons cannot be pressed twice
    val answeringId: Long? = null,
    // Message shown under the request that could not be answered
    val errorFriendshipId: Long? = null,
    @StringRes val errorMessage: Int? = null
)
