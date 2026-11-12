package pe.greenminds.ecomind.users.interfaces.shareprofile

data class ShareProfileUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val name: String = "",
    // True after "Copy link", to show the confirmation under the buttons
    val linkCopied: Boolean = false
)
