package pe.greenminds.ecomind.users.interfaces.commitment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.users.application.SaveCommitmentUseCase
import pe.greenminds.ecomind.users.domain.model.PersonalCommitment
import javax.inject.Inject

@HiltViewModel
class CommitmentViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val saveCommitment: SaveCommitmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommitmentUiState())
    val state: StateFlow<CommitmentUiState> = _state.asStateFlow()

    init {
        loadCommitment()
    }

    fun onTextChange(text: String) {
        // The field does not accept more characters than the limit
        if (text.length > PersonalCommitment.MAX_LENGTH) return

        _state.update { currentState ->
            currentState.copy(text = text, textError = null, saveFailed = false)
        }
    }

    fun save() {
        val current = _state.value
        if (current.isSaving) return

        // The same rule is checked again by the use case
        val text = current.text.trim()
        val textError = when {
            text.isEmpty() -> R.string.commitment_required
            !PersonalCommitment.isValid(text) -> R.string.commitment_length
            else -> null
        }
        if (textError != null) {
            _state.update { it.copy(textError = textError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveFailed = false) }

            saveCommitment(text)
                .onSuccess {
                    _state.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    _state.update { it.copy(isSaving = false, saveFailed = true) }
                }
        }
    }

    // When editing, the field starts with the commitment the user already has
    private fun loadCommitment() {
        viewModelScope.launch {
            val commitment = getCurrentProfile().getOrNull()?.commitment

            _state.update { currentState ->
                currentState.copy(isLoading = false, text = commitment ?: "")
            }
        }
    }
}
