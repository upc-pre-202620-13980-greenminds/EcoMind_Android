package pe.greenminds.ecomind.users.interfaces.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.PersonName
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.users.application.UpdateProfileNameUseCase
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val updateProfileName: UpdateProfileNameUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun onNameChange(name: String) {
        _state.update { currentState ->
            currentState.copy(name = name, nameError = null, saveFailed = false)
        }
    }

    fun save() {
        val current = _state.value
        if (current.isSaving) return

        // The same rule is checked again by the use case and by the web services
        val name = current.name.trim()
        val nameError = when {
            name.isEmpty() -> R.string.edit_profile_name_required
            !PersonName.isValid(name) -> R.string.edit_profile_name_length
            else -> null
        }
        if (nameError != null) {
            _state.update { it.copy(nameError = nameError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveFailed = false) }

            updateProfileName(name)
                .onSuccess {
                    _state.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    _state.update { it.copy(isSaving = false, saveFailed = true) }
                }
        }
    }

    // The field starts with the name the profile has now
    private fun loadProfile() {
        viewModelScope.launch {
            val profile = getCurrentProfile().getOrNull()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    name = profile?.name ?: "",
                    socialRole = profile?.socialRole
                )
            }
        }
    }
}
