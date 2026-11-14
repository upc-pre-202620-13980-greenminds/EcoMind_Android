package pe.greenminds.ecomind.shared.infrastructure.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import pe.greenminds.ecomind.BuildConfig

class Connectivity(context: Context) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val usesLocalApi = BuildConfig.DEBUG && BuildConfig.API_BASE_URL.startsWith("http://")

    val connected: Boolean
        get() = manager.getNetworkCapabilities(manager.activeNetwork)?.let { capabilities ->
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (usesLocalApi || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } == true

    val changes = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(connected) }
            override fun onLost(network: Network) { trySend(connected) }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(connected)
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        trySend(connected)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}

@Composable
fun isOnline(): Boolean {
    val context = LocalContext.current.applicationContext
    val connectivity = remember(context) { Connectivity(context) }
    val connected by connectivity.changes.collectAsStateWithLifecycle(connectivity.connected)
    return connected
}
