package pe.greenminds.ecomind.shared.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemBalanceStore @Inject constructor() {
    private val _balance = MutableStateFlow<Int?>(null)
    val balance: StateFlow<Int?> = _balance.asStateFlow()

    fun update(balance: Int) {
        _balance.value = balance
    }
}
