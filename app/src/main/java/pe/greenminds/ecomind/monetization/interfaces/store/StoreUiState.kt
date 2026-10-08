package pe.greenminds.ecomind.monetization.interfaces.store

import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector

enum class StoreCategory {
    COSMETICS,
    BOOSTS,
    GEMS
}

data class StoreUiState(
    val isLoading: Boolean = true,
    val selectedCategory: StoreCategory = StoreCategory.COSMETICS,
    val gemBalance: Int = 0,
    val insufficientRequiredGems: Int? = null,
    val showPurchaseComingSoon: Boolean = false,
    val items: List<StoreItem> = emptyList(),
    val multipliers: List<Multiplier> = emptyList(),
    val protectors: List<StreakProtector> = emptyList(),
    val gemPackages: List<GemPackage> = emptyList()
)
