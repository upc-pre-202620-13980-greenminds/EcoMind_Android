package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.monetization.application.GetStoreItemsUseCase
import pe.greenminds.ecomind.monetization.application.GetBoostItemsUseCase
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreItems: GetStoreItemsUseCase,
    private val getBoostItems: GetBoostItemsUseCase,
    private val getCurrentProfile: GetCurrentProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    init {
        loadStore()
    }

    // Only the main screen is built: there are no error or empty states yet,
    // so a failure leaves the list without items
    fun selectCategory(category: StoreCategory) {
        _state.update {
            it.copy(selectedCategory = category, insufficientRequiredGems = null)
        }
    }

    fun attemptPurchase(priceInGems: Int) {
        val currentBalance = _state.value.gemBalance
        if (currentBalance < priceInGems) {
            _state.update {
                it.copy(
                    insufficientRequiredGems = priceInGems,
                    showPurchaseComingSoon = false
                )
            }
            viewModelScope.launch {
                delay(ERROR_DURATION_MILLIS)
                _state.update { state ->
                    if (state.insufficientRequiredGems == priceInGems) {
                        state.copy(insufficientRequiredGems = null)
                    } else {
                        state
                    }
                }
            }
        } else {
            _state.update {
                it.copy(
                    insufficientRequiredGems = null,
                    showPurchaseComingSoon = true
                )
            }
        }
    }

    fun dismissPurchaseNotice() {
        _state.update { it.copy(showPurchaseComingSoon = false) }
    }

    private fun loadStore() {
        viewModelScope.launch {
            val items = getStoreItems().getOrDefault(emptyList())
            val boosts = getBoostItems().getOrNull()
            val profile = getCurrentProfile().getOrNull()

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    gemBalance = profile?.gemBalance ?: 0,
                    items = items,
                    multipliers = boosts?.multipliers.orEmpty(),
                    protectors = boosts?.protectors.orEmpty()
                )
            }
        }
    }

    private companion object {
        const val ERROR_DURATION_MILLIS = 4_000L
    }
}
