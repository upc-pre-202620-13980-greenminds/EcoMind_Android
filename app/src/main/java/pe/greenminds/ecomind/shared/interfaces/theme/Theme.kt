package pe.greenminds.ecomind.shared.interfaces.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = EcoGreen,
    onPrimary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = FieldGray,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = SurfaceTint,
    surfaceContainer = SurfaceSoft,
    outlineVariant = CardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA0D384),
    onPrimary = Color(0xFF173A08),
    background = Color(0xFF121610),
    onBackground = Color(0xFFE3E8DE),
    surface = Color(0xFF121610),
    onSurface = Color(0xFFE3E8DE),
    surfaceVariant = Color(0xFF252E20),
    onSurfaceVariant = Color(0xFFC1CBB8),
    outlineVariant = Color(0xFF424D3B)
)

@Composable
fun EcoMindTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
