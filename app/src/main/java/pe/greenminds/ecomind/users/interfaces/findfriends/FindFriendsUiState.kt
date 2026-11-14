package pe.greenminds.ecomind.users.interfaces.findfriends

import androidx.annotation.StringRes
import pe.greenminds.ecomind.users.domain.model.UserSearchResult

data class FindFriendsUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val results: List<UserSearchResult> = emptyList(),
    // User whose request is being sent, so their action cannot be pressed twice
    val sendingToUserId: Long? = null,
    // Message shown under the row of the user the request could not be sent to
    val errorUserId: Long? = null,
    @StringRes val errorMessage: Int? = null
)
