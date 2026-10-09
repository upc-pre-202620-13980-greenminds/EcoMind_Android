package pe.greenminds.ecomind.quests.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestPublicationStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestReward
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.presentation.states.QuestsUiState
import pe.greenminds.ecomind.quests.presentation.viewmodel.QuestsViewModel
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark
import kotlin.math.absoluteValue

private const val QUEST_TILE_ASPECT_RATIO = 142f / 137f
private val QUEST_SCREEN_HORIZONTAL_PADDING = 28.dp
private val QUEST_COLUMN_GAP = 30.dp
private val QUEST_ROW_GAP = 30.dp
private val QUEST_PAGE_GAP = 24.dp
private val QUEST_EDGE_FADE_WIDTH = 24.dp

@Composable
fun QuestsScreen(
    onOpenCategories: () -> Unit,
    onOpenQuest: (Long) -> Unit,
    onOpenLearning: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuestsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    QuestsContent(
        state = state,
        onOpenCategories = onOpenCategories,
        onOpenQuest = onOpenQuest,
        onOpenLearning = onOpenLearning,
        modifier = modifier
    )
}

@Composable
private fun QuestsContent(
    state: QuestsUiState,
    onOpenCategories: () -> Unit,
    onOpenQuest: (Long) -> Unit,
    onOpenLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryQuests = state.quests.filter { it.category == QuestCategory.ENERGY }
    val pages = categoryQuests.chunked(4).ifEmpty { listOf(emptyList()) }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        PillButton(
            text = stringResource(R.string.main_menu_energy),
            onClick = onOpenCategories,
            faceColor = SunYellow,
            baseColor = SunYellowDark,
            iconRes = R.drawable.ic_lightbulb,
            modifier = Modifier.padding(horizontal = QUEST_SCREEN_HORIZONTAL_PADDING)
        )

        Spacer(modifier = Modifier.height(40.dp))
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .height(320.dp)
                    .padding(120.dp)
            )

            state.hasError -> Text(
                text = stringResource(R.string.quest_list_error),
                modifier = Modifier.height(320.dp)
            )

            else -> BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val contentWidth = maxWidth - QUEST_SCREEN_HORIZONTAL_PADDING -
                    QUEST_SCREEN_HORIZONTAL_PADDING
                val tileWidth = (contentWidth - QUEST_COLUMN_GAP) / 2
                val tileHeight = tileWidth / QUEST_TILE_ASPECT_RATIO
                val pageHeight = tileHeight + tileHeight + QUEST_ROW_GAP
                val backgroundColor = MaterialTheme.colorScheme.background

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pageHeight)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = QUEST_PAGE_GAP,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val pageOffset = (
                            (pagerState.currentPage - pageIndex) +
                                pagerState.currentPageOffsetFraction
                            ).absoluteValue.coerceIn(0f, 1f)

                        QuestPage(
                            quests = pages[pageIndex],
                            onOpenQuest = onOpenQuest,
                            modifier = Modifier.graphicsLayer {
                                alpha = 1f - (pageOffset * 0.28f)
                                val scale = 1f - (pageOffset * 0.03f)
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                    }

                    CarouselEdgeFade(
                        backgroundColor = backgroundColor,
                        fadeFromStart = true,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    CarouselEdgeFade(
                        backgroundColor = backgroundColor,
                        fadeFromStart = false,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(pages.size) { pageIndex ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (pageIndex == pagerState.currentPage) {
                                Color(0xFF999999)
                            } else {
                                Color(0xFFD0D0D0)
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        PillButton(
            text = stringResource(R.string.main_menu_learn_more),
            onClick = onOpenLearning,
            faceColor = SkyBlue,
            baseColor = SkyBlueDark,
            iconRes = R.drawable.ic_lightbulb,
            modifier = Modifier.padding(horizontal = QUEST_SCREEN_HORIZONTAL_PADDING)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuestPage(
    quests: List<Quest>,
    onOpenQuest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(QUEST_ROW_GAP),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = QUEST_SCREEN_HORIZONTAL_PADDING)
    ) {
        repeat(2) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(QUEST_COLUMN_GAP),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(2) { columnIndex ->
                    val quest = quests.getOrNull(rowIndex * 2 + columnIndex)
                    if (quest != null) {
                        QuestTile(
                            theme = quest.theme,
                            contentDescription = quest.title,
                            onClick = { onOpenQuest(quest.id) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(QUEST_TILE_ASPECT_RATIO)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(4f)
                                .aspectRatio(QUEST_TILE_ASPECT_RATIO)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselEdgeFade(
    backgroundColor: Color,
    fadeFromStart: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (fadeFromStart) {
        listOf(backgroundColor, backgroundColor.copy(alpha = 0f))
    } else {
        listOf(backgroundColor.copy(alpha = 0f), backgroundColor)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(QUEST_EDGE_FADE_WIDTH)
            .background(Brush.horizontalGradient(colors))
    )
}

@Preview(showBackground = true, widthDp = 448, heightDp = 850)
@Composable
private fun QuestsContentPreview() {
    EcoMindTheme {
        QuestsContent(
            state = QuestsUiState(
                quests = listOf(
                    previewQuest(1, QuestTheme.COLLABORATIVE),
                    previewQuest(2, QuestTheme.CHECKBOX),
                    previewQuest(3, QuestTheme.CHECKBOX),
                    previewQuest(4, QuestTheme.MINIGAME)
                ),
                isLoading = false
            ),
            onOpenCategories = {},
            onOpenQuest = {},
            onOpenLearning = {}
        )
    }
}

private fun previewQuest(id: Long, theme: QuestTheme) = Quest(
    id = id,
    versionGroupId = id,
    versionNumber = 1,
    publicationStatus = QuestPublicationStatus.PUBLISHED,
    minigameId = null,
    title = "Preview quest $id",
    description = "Quest used only by the Compose preview",
    category = QuestCategory.ENERGY,
    type = QuestType.ACTIVITIES,
    reward = QuestReward(gems = 10, ecopoints = 20),
    targetAge = null,
    estimatedMinutes = 10,
    theme = theme,
    assignedDate = null,
    imageUrl = null
)
