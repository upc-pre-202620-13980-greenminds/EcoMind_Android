package pe.greenminds.ecomind.shared.interfaces.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EcoGreen,
    onPrimary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = FieldGray,
    onSurfaceVariant = HintGray,
    outlineVariant = DividerGray
)

// Provisional values, see Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = EcoGreen,
    onPrimary = White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outlineVariant = DarkCardBorder
)

// Colors of the design that the Material color scheme does not have. A screen that reads
// them from EcoMindTheme.colors changes with the theme; one that uses the constants of
// Color.kt does not.
@Immutable
data class EcoMindColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBorder: Color,
    val rowDivider: Color,
    val surfaceTint: Color,
    val statusSurface: Color,
    val errorRed: Color,
    val errorText: Color,
    val errorSurface: Color,
    val requiredRed: Color,
    val placeholder: Color,
    val fieldBorder: Color,
    val radioBorder: Color
)

private val LightEcoMindColors = EcoMindColors(
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    cardBorder = CardBorder,
    rowDivider = RowDivider,
    surfaceTint = SurfaceTint,
    statusSurface = StatusSurface,
    errorRed = ErrorRed,
    errorText = ErrorText,
    errorSurface = ErrorSurface,
    requiredRed = RequiredRed,
    placeholder = PlaceholderGray,
    fieldBorder = FieldBorderGreen,
    radioBorder = RadioBorder
)

private val DarkEcoMindColors = EcoMindColors(
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    cardBorder = DarkCardBorder,
    rowDivider = DarkRowDivider,
    surfaceTint = DarkSurfaceTint,
    statusSurface = DarkStatusSurface,
    errorRed = DarkErrorRed,
    errorText = DarkErrorText,
    errorSurface = DarkErrorSurface,
    requiredRed = DarkErrorRed,
    placeholder = DarkPlaceholderGray,
    fieldBorder = DarkFieldBorder,
    radioBorder = DarkRadioBorder
)

private val LocalEcoMindColors = staticCompositionLocalOf { LightEcoMindColors }

// The theme comes from Settings (frame 74); light is the default, as in the design
@Composable
fun EcoMindTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalEcoMindColors provides if (darkTheme) DarkEcoMindColors else LightEcoMindColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Same idea as MaterialTheme.colorScheme: EcoMindTheme.colors.textPrimary
object EcoMindTheme {
    val colors: EcoMindColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEcoMindColors.current
}
