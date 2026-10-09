package pe.greenminds.ecomind

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.res.Configuration
import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import pe.greenminds.ecomind.main.MainShell
import pe.greenminds.ecomind.settings.application.ObservePreferencesUseCase
import pe.greenminds.ecomind.settings.domain.model.AppPreferences
import pe.greenminds.ecomind.settings.domain.model.AppTheme
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var observePreferences: ObservePreferencesUseCase

    private val localNetworkPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(this, R.string.local_backend_permission_required, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by remember { observePreferences() }
                .collectAsStateWithLifecycle(initialValue = AppPreferences())
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = preferences.theme != AppTheme.DARK
                    isAppearanceLightNavigationBars = preferences.theme != AppTheme.DARK
                }
            }
            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val localizedContext = remember(context, configuration, preferences.language) {
                ContextThemeWrapper(context, 0).apply {
                    applyOverrideConfiguration(Configuration(configuration).apply {
                        setLocale(Locale.forLanguageTag(preferences.language.tag))
                    })
                }
            }
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                EcoMindTheme(darkTheme = preferences.theme == AppTheme.DARK) {
                    MainShell()
                }
            }
        }
        // API 37 protects LAN access, including the emulator's development host.
        // This permission is declared only by the debug manifest.
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 37 && savedInstanceState == null &&
            Uri.parse(BuildConfig.API_BASE_URL).host == "10.0.2.2" &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
            localNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }
}
