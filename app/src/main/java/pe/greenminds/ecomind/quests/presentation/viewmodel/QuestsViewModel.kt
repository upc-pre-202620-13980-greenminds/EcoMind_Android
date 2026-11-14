package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.usecase.GetQuestsUseCase
import pe.greenminds.ecomind.quests.presentation.states.QuestsUiState
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStore

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val getQuests: GetQuestsUseCase,
    private val offlineQuests: OfflineQuestStore
) : ViewModel() {

    private val _state = MutableStateFlow(QuestsUiState())
    val state: StateFlow<QuestsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val account = offlineQuests.session() ?: return@launch
            offlineQuests.observe(account.accountId).collect { quests ->
                _state.update { it.copy(savedQuests = quests) }
            }
        }
    }

    fun loadQuests(online: Boolean = true) {
        loadJob?.cancel()
        if (!online) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, hasError = false) }
            getQuests()
                .onSuccess { quests ->
                    _state.update { it.copy(quests = quests, isLoading = false) }
                    try {
                        offlineQuests.prepare(quests)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        // Existing downloads are still available if a refresh is interrupted.
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, hasError = true) }
                }
        }
    }
}
