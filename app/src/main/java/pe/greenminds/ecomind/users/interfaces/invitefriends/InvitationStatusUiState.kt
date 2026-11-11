package pe.greenminds.ecomind.users.interfaces.invitefriends

import pe.greenminds.ecomind.users.domain.model.InvitationDetails

data class InvitationStatusUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val invitation: InvitationDetails? = null
)
