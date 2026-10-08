package pe.greenminds.ecomind.gamification.interfaces.ranking

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.Ranking
import pe.greenminds.ecomind.gamification.domain.model.RankingEntry
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.components.SegmentedTabs
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun RankingScreen(viewModel: RankingViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    RankingContent(
        state = state,
        onTypeSelected = viewModel::onTypeSelected,
        onPeriodSelected = viewModel::onPeriodSelected,
        onRetry = viewModel::load
    )
}

@Composable
private fun RankingContent(
    state: RankingUiState,
    onTypeSelected: (RankingType) -> Unit,
    onPeriodSelected: (RankingPeriod) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.ranking_title),
            style = interTextStyle(20, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp)
                .semantics { heading() }
        )

        if (state.isLocked) {
            // Without activity there is nothing to filter, so tabs and chips are not shown
            RankingMessage(
                title = stringResource(R.string.ranking_locked_title),
                message = stringResource(R.string.ranking_locked_message),
                titleSizeSp = 19,
                modifier = Modifier.weight(1f)
            )
        } else {
            SegmentedTabs(
                options = RankingType.entries.map { stringResource(typeLabel(it)) },
                selectedIndex = state.selectedType.ordinal,
                onOptionSelected = { index -> onTypeSelected(RankingType.entries[index]) }
            )
            PeriodChips(
                options = RankingPeriod.entries.map { stringResource(periodLabel(it)) },
                selectedIndex = state.selectedPeriod.ordinal,
                onOptionSelected = { index -> onPeriodSelected(RankingPeriod.entries[index]) },
                modifier = Modifier.padding(top = 4.dp)
            )

            val ranking = state.ranking
            when {
                state.isLoading -> {
                    val loadingDescription = stringResource(R.string.loading)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics { contentDescription = loadingDescription }
                        )
                    }
                }

                // Tabs and chips stay on screen, so another ranking can be tried
                state.hasError || ranking == null -> {
                    RankingMessage(
                        title = stringResource(R.string.ranking_error_title),
                        message = stringResource(R.string.ranking_error_message),
                        titleSizeSp = 19,
                        modifier = Modifier.weight(1f)
                    )
                    PillButton(
                        text = stringResource(R.string.ranking_try_again),
                        onClick = onRetry,
                        faceColor = EcoGreen,
                        baseColor = EcoGreenDark,
                        height = 48.dp,
                        textStyle = interTextStyle(16, FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                ranking.entries.isEmpty() -> RankingMessage(
                    title = stringResource(R.string.ranking_empty),
                    message = null,
                    titleSizeSp = 16,
                    modifier = Modifier.weight(1f)
                )

                else -> RankingList(
                    ranking = ranking,
                    type = state.selectedType,
                    period = state.selectedPeriod,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RankingList(
    ranking: Ranking,
    type: RankingType,
    period: RankingPeriod,
    modifier: Modifier = Modifier
) {
    val isFamilies = type == RankingType.FAMILIES
    val currentEntry = ranking.currentEntry

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Shown only when the user or their family has ecopoints in the period
        if (currentEntry != null) {
            Spacer(modifier = Modifier.height(4.dp))
            CurrentPositionCard(
                label = stringResource(
                    if (isFamilies) R.string.ranking_your_family else R.string.ranking_your_position
                ),
                entry = currentEntry
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp)
        ) {
            Text(
                text = stringResource(listTitle(type), stringResource(periodPhrase(period))),
                style = interTextStyle(16, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            Text(
                text = stringResource(R.string.ranking_points_header),
                style = interTextStyle(12),
                color = TextSecondary,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        ranking.entries.forEach { entry ->
            RankingRow(
                entry = entry,
                displayName = displayNameOf(entry, isFamilies)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// "Alex Green (You)" for the user and "Green Home (Your family)" for their family
@Composable
private fun displayNameOf(entry: RankingEntry, isFamilies: Boolean): String {
    if (!entry.isCurrentUser) return entry.displayName

    return stringResource(
        if (isFamilies) R.string.ranking_name_your_family else R.string.ranking_name_you,
        entry.displayName
    )
}

@Composable
private fun RankingMessage(
    title: String,
    message: String?,
    titleSizeSp: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // The screen reader announces the message as soon as it appears
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            Text(
                text = title,
                style = interTextStyle(titleSizeSp, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (message != null) {
                Text(
                    text = message,
                    style = interTextStyle(13),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@StringRes
private fun typeLabel(type: RankingType): Int {
    return when (type) {
        RankingType.LOCAL -> R.string.ranking_type_local
        RankingType.GLOBAL -> R.string.ranking_type_global
        RankingType.FRIENDS -> R.string.ranking_type_friends
        RankingType.FAMILIES -> R.string.ranking_type_families
    }
}

@StringRes
private fun periodLabel(period: RankingPeriod): Int {
    return when (period) {
        RankingPeriod.DAILY -> R.string.ranking_period_daily
        RankingPeriod.WEEKLY -> R.string.ranking_period_weekly
        RankingPeriod.MONTHLY -> R.string.ranking_period_monthly
        RankingPeriod.ALL_TIME -> R.string.ranking_period_all_time
    }
}

// Second half of the title of the list: "This week", "This month"...
@StringRes
private fun periodPhrase(period: RankingPeriod): Int {
    return when (period) {
        RankingPeriod.DAILY -> R.string.ranking_phrase_today
        RankingPeriod.WEEKLY -> R.string.ranking_phrase_week
        RankingPeriod.MONTHLY -> R.string.ranking_phrase_month
        RankingPeriod.ALL_TIME -> R.string.ranking_phrase_all_time
    }
}

@StringRes
private fun listTitle(type: RankingType): Int {
    return when (type) {
        RankingType.LOCAL, RankingType.GLOBAL -> R.string.ranking_list_top
        RankingType.FRIENDS -> R.string.ranking_list_friends
        RankingType.FAMILIES -> R.string.ranking_list_families
    }
}

private val previewEntries = listOf(
    RankingEntry(1, 3L, "EcoMind", 34, isCurrentUser = false),
    RankingEntry(2, 1L, "EcoMind", 16, isCurrentUser = true)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun RankingContentPreview() {
    EcoMindTheme {
        RankingContent(
            state = RankingUiState(
                isLoading = false,
                ranking = Ranking(entries = previewEntries, currentEntry = previewEntries[1])
            ),
            onTypeSelected = {},
            onPeriodSelected = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun RankingContentLockedPreview() {
    EcoMindTheme {
        RankingContent(
            state = RankingUiState(isLoading = false, isLocked = true),
            onTypeSelected = {},
            onPeriodSelected = {},
            onRetry = {}
        )
    }
}
