package pe.greenminds.ecomind.quests.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.shared.interfaces.theme.ChipGray
import pe.greenminds.ecomind.shared.interfaces.theme.ChipGreen
import pe.greenminds.ecomind.shared.interfaces.theme.ChipPurple
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
internal fun QuestMetadataChips(
    quest: Quest,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp)
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (quest.category) {
            QuestCategory.ENERGY -> QuestChip(stringResource(R.string.quest_category_energy).lowercase(), SunYellow)
            QuestCategory.WATER -> QuestChip(stringResource(R.string.quest_category_water).lowercase(), SkyBlue)
            QuestCategory.RECYCLE -> QuestChip(stringResource(R.string.quest_category_recycle).lowercase(), LeafGreen)
        }
        quest.estimatedMinutes?.let { minutes ->
            QuestChip(stringResource(R.string.quest_chip_minutes, minutes), ChipGray)
        }
        QuestChip(stringResource(R.string.quest_chip_ecopoints, quest.reward.ecopoints), ChipGreen)
        val gems = quest.reward.gems
        if (gems > 0) {
            QuestChip(pluralStringResource(R.plurals.quest_chip_gems, gems, gems), SkyBlue)
        }
        if (quest.type == QuestType.COLLABORATIVE || quest.theme == QuestTheme.COLLABORATIVE) {
            QuestChip(stringResource(R.string.quest_theme_collaborative), ChipPurple)
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
