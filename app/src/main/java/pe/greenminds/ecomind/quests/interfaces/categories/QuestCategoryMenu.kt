package pe.greenminds.ecomind.quests.interfaces.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.interfaces.navigation.QuestSearchRoute
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.CoralRed
import pe.greenminds.ecomind.shared.interfaces.theme.CoralRedDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreen
import pe.greenminds.ecomind.shared.interfaces.theme.LeafGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark
import pe.greenminds.ecomind.shared.interfaces.theme.White

// Menu drawn over the main menu. Each button opens the quest list with a filter:
// onOpenList receives the route that carries it.
@Composable
fun QuestCategoryMenu(
    onOpenSearch: (QuestSearchRoute) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val closeLabel = stringResource(R.string.quest_close_menu)

    Box(
        modifier = modifier
            .fillMaxSize()
            // Lets the main menu show through, as in the design
            .background(White.copy(alpha = 0.63f))
            // Tapping outside the buttons closes the menu; no ripple over the whole screen
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = closeLabel,
                onClick = onDismiss
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 28.dp)
        ) {
            PillButton(
                text = stringResource(R.string.quest_category_energy),
                onClick = { onOpenSearch(QuestSearchRoute(category = QuestCategory.ENERGY.name)) },
                faceColor = SunYellow,
                baseColor = SunYellowDark,
                iconRes = R.drawable.ic_lightbulb
            )
            // The water drop icon has not been exported yet
            PillButton(
                text = stringResource(R.string.quest_category_water),
                onClick = { onOpenSearch(QuestSearchRoute(category = QuestCategory.WATER.name)) },
                faceColor = SkyBlue,
                baseColor = SkyBlueDark
            )
            PillButton(
                text = stringResource(R.string.quest_category_recycle),
                onClick = { onOpenSearch(QuestSearchRoute(category = QuestCategory.RECYCLE.name)) },
                faceColor = LeafGreen,
                baseColor = LeafGreenDark,
                iconRes = R.drawable.ic_category_recycle
            )
            PillButton(
                text = stringResource(R.string.quest_daily_quest),
                onClick = { onOpenSearch(QuestSearchRoute(questType = QuestType.DAILY_QUEST.name)) },
                faceColor = CoralRed,
                baseColor = CoralRedDark,
                iconRes = R.drawable.ic_category_daily_quest
            )
            PillButton(
                text = stringResource(R.string.quest_search_placeholder),
                onClick = { onOpenSearch(QuestSearchRoute(focusSearch = true)) },
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                iconRes = R.drawable.ic_category_search
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun QuestCategoryMenuPreview() {
    EcoMindTheme {
        QuestCategoryMenu(onOpenSearch = {}, onDismiss = {})
    }
}
