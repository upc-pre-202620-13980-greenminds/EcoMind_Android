package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.annotation.StringRes

data class InviteFriendsUiState(
    // Code a friend shared, as the user typed or pasted it
    val code: String = "",
    @StringRes val codeError: Int? = null,
    val isSending: Boolean = false
)
