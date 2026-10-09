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
import pe.greenminds.ecomind.monetization.application.GetGemPackagesUseCase
import pe.greenminds.ecomind.monetization.application.GetGemBalanceUseCase
import pe.greenminds.ecomind.monetization.application.PurchaseCosmeticUseCase
import pe.greenminds.ecomind.monetization.application.PurchaseMultiplierUseCase
import pe.greenminds.ecomind.monetization.application.PurchaseStreakProtectorUseCase
import pe.greenminds.ecomind.monetization.application.SetCosmeticEquippedUseCase
import pe.greenminds.ecomind.monetization.domain.model.CosmeticOwnership
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.shared.application.GemBalanceStore
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreItems: GetStoreItemsUseCase,
    private val getBoostItems: GetBoostItemsUseCase,
    private val getGemPackages: GetGemPackagesUseCase,
    private val getGemBalance: GetGemBalanceUseCase,
    private val purchaseCosmetic: PurchaseCosmeticUseCase,
    private val purchaseMultiplier: PurchaseMultiplierUseCase,
    private val purchaseStreakProtector: PurchaseStreakProtectorUseCase,
    private val setCosmeticEquipped: SetCosmeticEquippedUseCase,
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val gemBalanceStore: GemBalanceStore
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

    fun selectCosmeticView(view: CosmeticView) {
        _state.update {
            it.copy(selectedCosmeticView = view, insufficientRequiredGems = null)
        }
    }

    fun onCosmeticAction(item: StoreItem) {
        when (item.ownership) {
            CosmeticOwnership.NOT_OWNED -> buyCosmetic(item)
            CosmeticOwnership.IN_INVENTORY -> changeEquipped(item, equipped = true)
            CosmeticOwnership.EQUIPPED -> changeEquipped(item, equipped = false)
        }
    }

    fun buyMultiplier(item: Multiplier) {
        buyBoost(item.priceInGems) { purchaseMultiplier(item.id) }
    }

    fun buyStreakProtector(item: StreakProtector) {
        buyBoost(item.priceInGems) { purchaseStreakProtector(item.id) }
    }

    fun dismissPurchaseNotice() {
        _state.update { it.copy(showPurchaseComingSoon = false) }
    }

    private fun buyCosmetic(item: StoreItem) {
        val price = item.cosmetic.priceInGems
        if (_state.value.gemBalance < price) {
            showInsufficientGems(price)
            return
        }

        viewModelScope.launch {
            purchaseCosmetic(item.cosmetic.id).onSuccess {
                val newBalance = getGemBalance()
                    .getOrDefault(_state.value.gemBalance - price)
                val refreshedItems = getStoreItems().getOrDefault(_state.value.items)
                gemBalanceStore.update(newBalance)
                _state.update {
                    it.copy(
                        gemBalance = newBalance,
                        items = refreshedItems,
                        insufficientRequiredGems = null
                    )
                }
            }
        }
    }

    private fun changeEquipped(item: StoreItem, equipped: Boolean) {
        viewModelScope.launch {
            setCosmeticEquipped(item.cosmetic.id, equipped).onSuccess {
                val refreshedItems = getStoreItems().getOrDefault(_state.value.items)
                _state.update { it.copy(items = refreshedItems) }
            }
        }
    }

    private fun showInsufficientGems(priceInGems: Int) {
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
    }

    private fun buyBoost(
        priceInGems: Int,
        purchase: suspend () -> Result<Unit>
    ) {
        if (_state.value.gemBalance < priceInGems) {
            showInsufficientGems(priceInGems)
            return
        }

        viewModelScope.launch {
            purchase().onSuccess {
                val newBalance = getGemBalance()
                    .getOrDefault(_state.value.gemBalance - priceInGems)
                gemBalanceStore.update(newBalance)
                _state.update {
                    it.copy(
                        gemBalance = newBalance,
                        insufficientRequiredGems = null
                    )
                }
            }
        }
    }

    private fun loadStore() {
        viewModelScope.launch {
            val items = getStoreItems().getOrDefault(emptyList())
            val boosts = getBoostItems().getOrNull()
            val gemPackages = getGemPackages().getOrDefault(emptyList())
            val profile = getCurrentProfile().getOrNull()
            val walletBalance = getGemBalance().getOrDefault(profile?.gemBalance ?: 0)
            gemBalanceStore.update(walletBalance)

            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    userId = profile?.id,
                    gemBalance = walletBalance,
                    items = items,
                    multipliers = boosts?.multipliers.orEmpty(),
                    protectors = boosts?.protectors.orEmpty(),
                    gemPackages = gemPackages
                )
            }
        }
    }

    private companion object {
        const val ERROR_DURATION_MILLIS = 4_000L
    }
}
