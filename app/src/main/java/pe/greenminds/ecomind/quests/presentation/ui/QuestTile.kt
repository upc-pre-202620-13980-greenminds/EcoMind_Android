package pe.greenminds.ecomind.quests.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.presentation.ui.theme.LightQuestColorScheme
import pe.greenminds.ecomind.quests.presentation.ui.theme.QuestColorScheme
import pe.greenminds.ecomind.quests.presentation.ui.theme.QuestTileColors
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

private val QuestTileShape = RoundedCornerShape(46.dp)

@Composable
fun QuestTile(
    theme: QuestTheme,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: QuestColorScheme = LightQuestColorScheme
) {
    val tileColors = colors.colorsFor(theme)
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        QuestTileBase(
            color = tileColors.base,
            modifier = Modifier.matchParentSize()
        )

        QuestTileFace(
            colors = tileColors,
            theme = theme,
            isPressed = isPressed,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun QuestTileBase(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .clip(QuestTileShape)
            .background(color)
    )
}

@Composable
private fun QuestTileFace(
    colors: QuestTileColors,
    theme: QuestTheme,
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(bottom = 10.dp)
            .offset(y = if (isPressed) 8.dp else -6.dp)
            .clip(QuestTileShape)
            .background(colors.top)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 18.dp)
                .clip(QuestTileShape)
                .background(colors.face)
        )

        Text(
            text = theme.name,
            color = Color.White
        )
    }
}

@Preview(
    name = "Quest tile shape",
    showBackground = true,
    widthDp = 220,
    heightDp = 220
)
@Composable
private fun QuestTilePreview() {
    EcoMindTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            QuestTile(
                theme = QuestTheme.CHECKBOX,
                contentDescription = QuestTheme.CHECKBOX.name,
                onClick = {},
                modifier = Modifier.size(
                    width = 160.dp,
                    height = 155.dp
                )
            )
        }
    }
}

