package pe.greenminds.ecomind.users.interfaces.friendprofile

import androidx.annotation.StringRes
import pe.greenminds.ecomind.users.domain.model.FriendProfile

data class FriendProfileUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val profile: FriendProfile? = null,
    // Frame 14: the confirmation card replaces "You are friends" and "Remove friend"
    val isConfirmingRemoval: Boolean = false,
    val isRemoving: Boolean = false,
    @StringRes val removeError: Int? = null,
    val isRemoved: Boolean = false
)
