package pe.greenminds.ecomind.quests.interfaces.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.White

private val CATEGORY_PILL_HEIGHT = 63.dp

@Composable
fun QuestCategoryMenu(
    expanded: Boolean,
    selectedFilter: QuestListFilter,
    onSelectFilter: (QuestListFilter) -> Unit,
    onOpenSearch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val closeLabel = stringResource(R.string.quest_close_menu)
    val optionStep = with(LocalDensity.current) { 79.dp.roundToPx() }
    val searchExtraDistance = with(LocalDensity.current) { 34.dp.roundToPx() }
    val availableFilters = QuestListFilter.entries.filter { it != selectedFilter }
    var animatedExpanded by remember { mutableStateOf(false) }
    var promotedFilter by remember { mutableStateOf<QuestListFilter?>(null) }
    val promotedIndex = availableFilters.indexOf(promotedFilter)
    val promotedTravel = if (promotedIndex >= 0) 79.dp * (promotedIndex + 1) else 0.dp
    val menuTransition = updateTransition(
        targetState = animatedExpanded,
        label = "category menu"
    )
    val promotedOffset by menuTransition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = 1f,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "promoted category offset"
    ) { isExpanded ->
        if (isExpanded) 0.dp else -promotedTravel
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            withFrameNanos { }
        }
        animatedExpanded = expanded
    }

    Box(modifier = modifier.fillMaxSize()) {
        menuTransition.AnimatedVisibility(
            visible = { it },
            enter = fadeIn(tween(durationMillis = 320)),
            exit = fadeOut(tween(durationMillis = 220))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White.copy(alpha = 0.63f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = closeLabel,
                        onClick = onDismiss
                    )
            )
        }

        Column(
            modifier = Modifier
                .zIndex(1f)
                .verticalScroll(
                    state = rememberScrollState(),
                    enabled = expanded
                )
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CATEGORY_PILL_HEIGHT)
                    .zIndex(2f)
            ) {
                menuTransition.AnimatedVisibility(
                    visible = { it },
                    enter = fadeIn(tween(durationMillis = 80)),
                    exit = fadeOut(tween(durationMillis = 1, delayMillis = 400)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    QuestCategoryPill(
                        filter = selectedFilter,
                        onClick = onDismiss
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                availableFilters.forEachIndexed { index, filter ->
                    val travelDistance = optionStep * (index + 1)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CATEGORY_PILL_HEIGHT)
                    ) {
                        menuTransition.AnimatedVisibility(
                            visible = { it },
                            enter = slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = 0.76f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialOffsetY = { -travelDistance }
                            ),
                            exit = slideOutVertically(
                                animationSpec = spring(
                                    dampingRatio = 1f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                targetOffsetY = { -travelDistance }
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (promotedFilter == filter) 0f else 1f)
                                .zIndex(0f)
                        ) {
                            QuestCategoryPill(
                                filter = filter,
                                onClick = {
                                    if (promotedFilter == null) {
                                        promotedFilter = filter
                                        onSelectFilter(filter)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                val searchTravelDistance =
                    optionStep * (availableFilters.size + 1) + searchExtraDistance
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CATEGORY_PILL_HEIGHT)
                ) {
                    menuTransition.AnimatedVisibility(
                        visible = { it },
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = 0.76f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetY = { -searchTravelDistance }
                        ),
                        exit = slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = 1f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            targetOffsetY = { -searchTravelDistance }
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f)
                    ) {
                        PillButton(
                            text = stringResource(R.string.quest_search_placeholder),
                            onClick = onOpenSearch,
                            faceColor = EcoGreen,
                            baseColor = EcoGreenDark,
                            iconRes = R.drawable.ic_category_search
                        )
                    }
                }
            }
        }

        promotedFilter?.let { filter ->
            QuestCategoryPill(
                filter = filter,
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 60.dp)
                    .offset(y = promotedTravel + promotedOffset)
                    .zIndex(3f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun QuestCategoryMenuPreview() {
    EcoMindTheme {
        QuestCategoryMenu(
            expanded = true,
            selectedFilter = QuestListFilter.ENERGY,
            onSelectFilter = {},
            onOpenSearch = {},
            onDismiss = {}
        )
    }
}
