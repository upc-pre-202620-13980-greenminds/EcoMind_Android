package pe.greenminds.ecomind.quests.interfaces.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.CoralRed
import pe.greenminds.ecomind.shared.interfaces.theme.CoralRedDark
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreen
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark

enum class QuestListFilter {
    ENERGY,
    WATER,
    RECYCLE,
    DAILY_QUEST
}

@Composable
fun QuestCategoryPill(
    filter: QuestListFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (filter) {
        QuestListFilter.ENERGY -> PillButton(
            text = stringResource(R.string.quest_category_energy),
            onClick = onClick,
            faceColor = SunYellow,
            baseColor = SunYellowDark,
            iconRes = R.drawable.ic_lightbulb,
            modifier = modifier
        )

        QuestListFilter.WATER -> PillButton(
            text = stringResource(R.string.quest_category_water),
            onClick = onClick,
            faceColor = SkyBlue,
            baseColor = SkyBlueDark,
            modifier = modifier
        )

        QuestListFilter.RECYCLE -> PillButton(
            text = stringResource(R.string.quest_category_recycle),
            onClick = onClick,
            faceColor = LeafGreen,
            baseColor = LeafGreenDark,
            iconRes = R.drawable.ic_category_recycle,
            modifier = modifier
        )

        QuestListFilter.DAILY_QUEST -> PillButton(
            text = stringResource(R.string.quest_daily_quest),
            onClick = onClick,
            faceColor = CoralRed,
            baseColor = CoralRedDark,
            iconRes = R.drawable.ic_category_daily_quest,
            modifier = modifier
        )
    }
}
