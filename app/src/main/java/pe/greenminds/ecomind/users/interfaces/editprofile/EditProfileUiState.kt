package pe.greenminds.ecomind.users.interfaces.editprofile

import androidx.annotation.StringRes
import pe.greenminds.ecomind.users.domain.model.SocialRole

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val socialRole: SocialRole? = null,
    @StringRes val nameError: Int? = null,
    val isSaving: Boolean = false,
    // The text the user wrote stays in the field when saving fails
    val saveFailed: Boolean = false,
    val isSaved: Boolean = false
)
