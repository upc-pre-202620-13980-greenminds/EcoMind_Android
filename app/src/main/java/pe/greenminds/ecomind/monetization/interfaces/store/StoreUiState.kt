package pe.greenminds.ecomind.monetization.interfaces.store

import pe.greenminds.ecomind.monetization.domain.model.StoreItem

data class StoreUiState(
    val isLoading: Boolean = true,
    val items: List<StoreItem> = emptyList()
)
