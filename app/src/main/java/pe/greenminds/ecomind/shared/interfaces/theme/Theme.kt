package pe.greenminds.ecomind.shared.interfaces.theme

import androidx.compose.material3.MaterialTheme
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
    onSurfaceVariant = HintGray,
    outlineVariant = DividerGray
)

// Only the light theme exists for now, so the system setting is not read
@Composable
fun EcoMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
