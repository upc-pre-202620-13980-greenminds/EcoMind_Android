package pe.greenminds.ecomind.users.interfaces.shareprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import javax.inject.Inject

@HiltViewModel
class ShareProfileViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShareProfileUiState())
    val state: StateFlow<ShareProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    // Copying to the clipboard needs the Android context, so the screen does it and reports here
    fun onLinkCopied() {
        _state.update { it.copy(linkCopied = true) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = getCurrentProfile().getOrNull()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    userId = profile?.id,
                    name = profile?.name ?: ""
                )
            }
        }
    }
}
