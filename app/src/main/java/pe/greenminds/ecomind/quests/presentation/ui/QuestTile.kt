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

    androidx.compose.foundation.layout.BoxWithConstraints(
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
        val edge = minOf(maxWidth, maxHeight)
        val shape = RoundedCornerShape(edge * .28f)
        QuestTileBase(
            color = tileColors.base,
            edge = edge, shape = shape,
            modifier = Modifier.matchParentSize()
        )

        QuestTileFace(
            colors = tileColors,
            edge = edge, shape = shape,
            theme = theme,
            isPressed = isPressed,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun QuestTileBase(
    color: Color,
    edge: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = edge * .075f)
            .clip(shape)
            .background(color)
    )
}

@Composable
private fun QuestTileFace(
    colors: QuestTileColors,
    edge: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    theme: QuestTheme,
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(bottom = edge * .065f)
            .offset(y = if (isPressed) edge * .05f else -edge * .04f)
            .clip(shape)
            .background(colors.top)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = edge * .115f)
                .clip(shape)
                .background(colors.face)
        )

        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(when (theme) {
                QuestTheme.CHECKBOX -> pe.greenminds.ecomind.R.drawable.ic_quest_checkbox
                QuestTheme.MINIGAME -> pe.greenminds.ecomind.R.drawable.ic_quest_minigame
                QuestTheme.COLLABORATIVE -> pe.greenminds.ecomind.R.drawable.ic_quest_collaborative
            }),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(.50f)
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


