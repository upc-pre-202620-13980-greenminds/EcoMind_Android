package pe.greenminds.ecomind.quests.interfaces.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.model.Quest
import pe.greenminds.ecomind.quests.domain.model.QuestCategory
import pe.greenminds.ecomind.quests.domain.model.QuestTheme
import pe.greenminds.ecomind.quests.domain.model.QuestType
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.ChipGray
import pe.greenminds.ecomind.shared.interfaces.theme.ChipGreen
import pe.greenminds.ecomind.shared.interfaces.theme.ChipPurple
import pe.greenminds.ecomind.shared.interfaces.theme.DividerGray
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun QuestCard(
    quest: Quest,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, DividerGray, shape)
            // The whole card opens the quest, so the small button is not the only touch target
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(12.dp)
    ) {
        // The chips scroll sideways when they do not fit, for example with a large font
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            QuestChip(
                text = stringResource(categoryLabel(quest.category)).lowercase(),
                color = categoryColor(quest.category)
            )
            QuestChip(
                text = stringResource(R.string.quest_chip_minutes, quest.minutes),
                color = ChipGray
            )
            QuestChip(
                text = stringResource(R.string.quest_chip_ecopoints, quest.ecopoints),
                color = ChipGreen
            )
            QuestChip(
                text = stringResource(themeLabel(quest.theme)),
                color = ChipPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            // Decorative: the chips already say the theme of the quest
            Image(
                painter = painterResource(tileFor(quest)),
                contentDescription = null,
                modifier = Modifier.size(width = 93.dp, height = 90.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = interTextStyle(15, FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = quest.description,
                    style = interTextStyle(11, FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .width(116.dp)
                ) {
                    PillButton(
                        text = stringResource(R.string.quest_view),
                        onClick = onOpen,
                        faceColor = SkyBlue,
                        baseColor = SkyBlueDark,
                        height = 32.dp,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestChip(text: String, color: Color) {
    Text(
        text = text,
        style = interTextStyle(10, FontWeight.Bold),
        color = White,
        maxLines = 1,
        modifier = Modifier
            .background(color, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@StringRes
fun categoryLabel(category: QuestCategory): Int {
    return when (category) {
        QuestCategory.ENERGY -> R.string.quest_category_energy
        QuestCategory.WATER -> R.string.quest_category_water
        QuestCategory.RECYCLE -> R.string.quest_category_recycle
    }
}

// Same colors as the buttons of the category menu
private fun categoryColor(category: QuestCategory): Color {
    return when (category) {
        QuestCategory.ENERGY -> SunYellow
        QuestCategory.WATER -> SkyBlue
        QuestCategory.RECYCLE -> LeafGreen
    }
}

@StringRes
private fun themeLabel(theme: QuestTheme): Int {
    return when (theme) {
        QuestTheme.CHECKBOX -> R.string.quest_theme_checkbox
        QuestTheme.MINIGAME -> R.string.quest_theme_minigame
        QuestTheme.COLLABORATIVE -> R.string.quest_theme_collaborative
    }
}

// Daily quests use the "play" tile; the rest use the tile of their theme
@DrawableRes
private fun tileFor(quest: Quest): Int {
    if (quest.type == QuestType.DAILY_QUEST) return R.drawable.img_tile_videos_default

    return when (quest.theme) {
        QuestTheme.CHECKBOX -> R.drawable.img_tile_quests_default
        QuestTheme.MINIGAME -> R.drawable.img_tile_minigames_default
        QuestTheme.COLLABORATIVE -> R.drawable.img_tile_collaborative_default
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun QuestCardPreview() {
    EcoMindTheme {
        QuestCard(
            quest = Quest(
                id = 1L,
                title = stringResource(R.string.app_name),
                description = stringResource(R.string.placeholder_message),
                category = QuestCategory.ENERGY,
                type = QuestType.COLLABORATIVE,
                theme = QuestTheme.COLLABORATIVE,
                gemReward = 30,
                ecopoints = 50,
                minutes = 60,
                recommendedAge = 8,
                imageUrl = null,
                minigameId = null
            ),
            onOpen = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
