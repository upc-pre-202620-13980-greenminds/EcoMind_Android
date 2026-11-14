package pe.greenminds.ecomind.users.interfaces.commitment

import androidx.annotation.StringRes

data class CommitmentUiState(
    val isLoading: Boolean = true,
    val text: String = "",
    @StringRes val textError: Int? = null,
    val isSaving: Boolean = false,
    // The text the user wrote stays in the field when saving fails
    val saveFailed: Boolean = false,
    val isSaved: Boolean = false
)
