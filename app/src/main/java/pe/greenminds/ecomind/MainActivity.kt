package pe.greenminds.ecomind

import android.content.res.Configuration
import android.os.Bundle
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
    }
}
