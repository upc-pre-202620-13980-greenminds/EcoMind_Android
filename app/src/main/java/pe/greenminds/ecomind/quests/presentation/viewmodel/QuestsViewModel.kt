package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.GetQuestsUseCase
import pe.greenminds.ecomind.quests.presentation.states.QuestsUiState
import javax.inject.Inject

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val getQuests: GetQuestsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QuestsUiState())
    val state: StateFlow<QuestsUiState> = _state.asStateFlow()

    init {
        loadQuests()
    }

    fun loadQuests() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, hasError = false) }
            getQuests()
                .onSuccess { quests ->
                    _state.update { it.copy(quests = quests, isLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, hasError = true) }
                }
        }
    }
}
