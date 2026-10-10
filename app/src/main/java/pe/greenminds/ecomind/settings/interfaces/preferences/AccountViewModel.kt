package pe.greenminds.ecomind.settings.interfaces.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.users.domain.model.UserProfile
import javax.inject.Inject

data class AccountUiState(val loading: Boolean = true, val profile: UserProfile? = null, val email: String = "", val error: Boolean = false)
@HiltViewModel
class AccountViewModel @Inject constructor(private val profile: GetCurrentProfileUseCase, private val sessions: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()
    init { load() }
    fun load() { viewModelScope.launch {
        _state.value = AccountUiState()
        try {
            val session = sessions.getSession().first()
            if (session == null || session.isExpired(System.currentTimeMillis())) { _state.value = AccountUiState(loading = false, error = true); return@launch }
            profile().onSuccess { _state.value = AccountUiState(false, it, session.email) }
                .onFailure { _state.value = AccountUiState(loading = false, error = true) }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (exception: Exception) { _state.value = AccountUiState(loading = false, error = true) }
    } }
}
