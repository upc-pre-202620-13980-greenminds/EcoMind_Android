package pe.greenminds.ecomind.quests.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme

data class QuestTileColors(
    val face: Color,
    val base: Color,
    val top: Color
)

data class QuestColorScheme(
    val checkbox: QuestTileColors,
    val minigame: QuestTileColors,
    val collaborative: QuestTileColors
) {
    fun colorsFor(theme: QuestTheme): QuestTileColors {
        return when (theme) {
            QuestTheme.CHECKBOX -> checkbox
            QuestTheme.MINIGAME -> minigame
            QuestTheme.COLLABORATIVE -> collaborative
        }
    }
}

val LightQuestColorScheme = QuestColorScheme(
    checkbox = QuestTileColors(
        face = Color(0xFF66D575),
        base = Color(0xFF159E67),
        top = Color(0xFF9BE5A5)
    ),
    minigame = QuestTileColors(
        face = Color(0xFF3FA8F5),
        base = Color(0xFF4B66DF),
        top = Color(0xFF83CAFF)
    ),
    collaborative = QuestTileColors(
        face = Color(0xFFFFC44D),
        base = Color(0xFFD9901F),
        top = Color(0xFFFFE079)
    )
)