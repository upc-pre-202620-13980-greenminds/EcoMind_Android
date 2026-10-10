package pe.greenminds.ecomind.notifications.interfaces.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.notifications.application.*
import pe.greenminds.ecomind.notifications.domain.model.NotificationSessionRequiredException
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val observe: ObserveNotificationsUseCase, private val read: ReadNotificationsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState(isSimulated = observe.isSimulated))
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()
    private var job: Job? = null
    init { load() }
    fun load() {
        job?.cancel()
        _state.update { it.copy(isLoading = true, hasError = false, sessionRequired = false) }
        job = viewModelScope.launch {
            observe().catch { error ->
                if (error is CancellationException) throw error
                _state.update { it.copy(isLoading = false, items = emptyList(), hasError = true, sessionRequired = error is NotificationSessionRequiredException) }
            }.collect { items -> _state.update { it.copy(isLoading = false, items = items) } }
        }
    }
    fun markAllRead() = markRead(null) {}
    fun markRead(id: String?, onSuccess: () -> Unit) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, hasError = false) }
            try { read(id).onSuccess { onSuccess() }.onFailure { error ->
                if (error is CancellationException) throw error
                _state.update { it.copy(hasError = true, sessionRequired = error is NotificationSessionRequiredException) }
            } } catch (cancelled: CancellationException) { throw cancelled }
            catch (exception: Exception) { _state.update { it.copy(hasError = true) } } finally { _state.update { it.copy(isSaving = false) } }
        }
    }
}
