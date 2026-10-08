package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.monetization.application.GetStoreItemsUseCase
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreItems: GetStoreItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    init {
        loadItems()
    }

    // Only the main screen is built: there are no error or empty states yet,
    // so a failure leaves the list without items
    private fun loadItems() {
        viewModelScope.launch {
            val items = getStoreItems().getOrDefault(emptyList())

            _state.update { currentState ->
                currentState.copy(isLoading = false, items = items)
            }
        }
    }
}
