package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.annotation.StringRes
import pe.greenminds.ecomind.users.domain.model.UserSearchResult

data class ChooseContactsUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    // Users the search found that can be invited, without the selected ones
    val contacts: List<UserSearchResult> = emptyList(),
    // Kept apart so a selected contact stays on screen while the user searches another name
    val selected: List<UserSearchResult> = emptyList(),
    val isSending: Boolean = false,
    @StringRes val errorMessage: Int? = null
)

// Where the screen goes after sending: one invitation opens its status, several open the requests
sealed interface ChooseContactsEvent {
    data class OpenStatus(val friendshipId: Long) : ChooseContactsEvent
    data object OpenRequests : ChooseContactsEvent
}
