package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.Community

data class CreateCommunityUiState(
    val isOpen: Boolean = false,
    val canCreate: Boolean = false,
    val isCheckingPermission: Boolean = true,
    val permissionError: String? = null,
    val name: String = "",
    val description: String = "",
    val topic: String = "",
    val limit: String = "",
    val imageUri: String? = null,
    val uploadedImageUrl: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val errorRes: Int? = null,
    val createdCommunity: Community? = null
)