package pe.greenminds.ecomind.notifications.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Number shown on the bell of the top bar. A singleton, like GemBalanceStore, so the inbox
// and the top bar always see the same value.
@Singleton
class UnreadNotificationsStore @Inject constructor() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun update(count: Int) {
        _count.value = count.coerceAtLeast(0)
    }

    fun clear() {
        _count.value = 0
    }
}
