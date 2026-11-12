package pe.greenminds.ecomind.community.infrastructure.implementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import pe.greenminds.ecomind.community.domain.repositories.CommunityLocationRepository
import pe.greenminds.ecomind.community.domain.valueobjects.LocalityException
import pe.greenminds.ecomind.community.domain.valueobjects.LocalityFailure
import kotlin.time.Duration.Companion.seconds

class AndroidCommunityLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : CommunityLocationRepository {

    override suspend fun getCurrentLocality(): Result<String> = try {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            throw LocalityException(LocalityFailure.PERMISSION_DENIED)
        }

        val manager = context.getSystemService(LocationManager::class.java)
            ?: throw LocalityException(LocalityFailure.LOCATION_UNAVAILABLE)

        if (!LocationManagerCompat.isLocationEnabled(manager)) {
            throw LocalityException(LocalityFailure.LOCATION_DISABLED)
        }

        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
        ).filter { provider ->
            provider in manager.allProviders &&
                    manager.isProviderEnabled(provider)
        }

        if (providers.isEmpty()) {
            throw LocalityException(LocalityFailure.LOCATION_UNAVAILABLE)
        }

        var location: Location? = null

        for (provider in providers) {
            location = withTimeoutOrNull(15.seconds) {
                currentLocation(manager, provider)
            }

            if (location != null) break
        }

        val currentLocation = location
            ?: throw LocalityException(LocalityFailure.LOCATION_UNAVAILABLE)
        val locality = withTimeoutOrNull(15.seconds) {
            resolveLocality(currentLocation)
        } ?: throw LocalityException(LocalityFailure.GEOCODING_UNAVAILABLE)


        Result.success(locality)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: SecurityException) {
        Result.failure(LocalityException(LocalityFailure.PERMISSION_DENIED))
    } catch (exception: LocalityException) {
        Result.failure(exception)
    } catch (exception: Exception) {
        Result.failure(LocalityException(LocalityFailure.LOCATION_UNAVAILABLE))
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()

        continuation.invokeOnCancellation {
            cancellationSignal.cancel()
        }

        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            cancellationSignal,
            ContextCompat.getMainExecutor(context)
        ) { location ->
            if (continuation.isActive) {
                continuation.resume(location)
            }
        }
    }

    private suspend fun resolveLocality(location: Location): String? {
        if (!Geocoder.isPresent()) {
            throw LocalityException(LocalityFailure.GEOCODING_UNAVAILABLE)
        }

        val geocoder = Geocoder(context, Locale.forLanguageTag("es-PE"))

        return try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<List<Address>> { continuation ->
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) {
                                    continuation.resume(addresses)
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) {
                                    continuation.resume(emptyList())
                                }
                            }
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ).orEmpty()
                }
            }

            addresses.firstOrNull()
                ?.locality
                ?.trim()
                ?.takeIf(String::isNotEmpty)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw LocalityException(LocalityFailure.GEOCODING_UNAVAILABLE)
        }
    }
}