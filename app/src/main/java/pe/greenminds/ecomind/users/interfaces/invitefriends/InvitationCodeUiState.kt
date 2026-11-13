package pe.greenminds.ecomind.users.interfaces.invitefriends

data class InvitationCodeUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    // Code without the space, as the web services return it
    val code: String? = null,
    val isCopied: Boolean = false
)
