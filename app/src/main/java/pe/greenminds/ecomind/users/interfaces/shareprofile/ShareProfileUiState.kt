package pe.greenminds.ecomind.users.interfaces.shareprofile

import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics

data class ShareProfileUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val name: String = "",
    // Same avatar as the profile header
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),
    // True after "Copy link", to show the confirmation under the buttons
    val linkCopied: Boolean = false
)
