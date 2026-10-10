package pe.greenminds.ecomind.settings.interfaces.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.settings.application.*
import pe.greenminds.ecomind.settings.domain.model.*
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    observe: ObservePreferencesUseCase,
    private val update: UpdatePreferencesUseCase,
    private val sessions: SessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PreferencesUiState())
    val state: StateFlow<PreferencesUiState> = _state.asStateFlow()
    init { viewModelScope.launch {
        observe().catch { exception ->
            if (exception is CancellationException) throw exception
            _state.update { it.copy(isLoading = false, hasError = true) }
        }.collect { preferences -> _state.update { it.copy(preferences = preferences, isLoading = false) } }
    } }
    fun language(value: AppLanguage) = save { update.language(value) }
    fun theme(value: AppTheme) = save { update.theme(value) }
    fun notification(category: NotificationCategory, enabled: Boolean) = save { update.notification(category, enabled) }
    private fun save(action: suspend () -> Result<Unit>) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, hasError = false) }
            try { action().onFailure { exception ->
                if (exception is CancellationException) throw exception
                _state.update { it.copy(hasError = true) }
            } } catch (cancelled: CancellationException) { throw cancelled }
            catch (exception: Exception) { _state.update { it.copy(hasError = true) } } finally { _state.update { it.copy(isSaving = false) } }
        }
    }
    fun signOut(onComplete: () -> Unit) = save {
        try { sessions.clearSession(); onComplete(); Result.success(Unit) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (exception: Exception) { Result.failure(exception) }
    }
}
