package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.presentation.states.CommunitySection
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState
import pe.greenminds.ecomind.community.presentation.viewmodel.CommunityViewModel
import pe.greenminds.ecomind.shared.interfaces.components.ComingSoonDialog
import pe.greenminds.ecomind.shared.interfaces.theme.Black
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.FadedGray
import pe.greenminds.ecomind.shared.interfaces.theme.HintGray
import pe.greenminds.ecomind.shared.interfaces.theme.MossGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SageGreen
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

@Composable
fun CommunityScreen(viewModel: CommunityViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    when {
        state.isLoadingCommunity -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.selectedCommunity == null -> {
            LocalCommunitySelectionScreen(
                state = state,
                onLocalityInputChange = viewModel::updateLocalityInput,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearch = viewModel::saveLocalityAndSearch,
                onEnter = viewModel::showCommunityDetails,
                onJoin = viewModel::joinPendingCommunity,
                onCancelDetails = viewModel::dismissCommunityDetails,
                onRetry = viewModel::retry
            )
        }

        else -> {
            CommunityContent(
                state = state,
                community = checkNotNull(state.selectedCommunity),
                onSectionSelected = viewModel::selectSection
            )
        }
    }
}

@Composable
private fun CommunityContent(
    state: CommunityUiState,
    community: Community,
    onSectionSelected: (CommunitySection) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showComingSoon by rememberSaveable { mutableStateOf(false) }
    val openComingSoon = { showComingSoon = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CommunitySelector(onOtherCommunities = openComingSoon)
            CommunityHeader(community)
            GoalCard(onClick = openComingSoon)
        }

        Spacer(modifier = Modifier.height(16.dp))
        CommunityTabs(
            selectedSection = state.selectedSection,
            onSectionSelected = onSectionSelected
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            when (state.selectedSection) {
                CommunitySection.EVENTS -> {
                    SearchRow(onAction = openComingSoon)

                    if (state.isLoadingEvents) {
                        val loadingDescription = stringResource(R.string.loading)
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 24.dp)
                                .semantics { contentDescription = loadingDescription }
                        )
                    } else {
                        state.events.forEach { event ->
                            CommunityEventCard(event = event, onAction = openComingSoon)
                        }
                    }
                }

                CommunitySection.AWARDS -> CommunityAwardsContent()
                CommunitySection.NEWS -> CommunityNewsContent()
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showComingSoon) {
        ComingSoonDialog(onDismiss = { showComingSoon = false })
    }
}

@Composable
internal fun CommunitySelector(
    onOtherCommunities: () -> Unit,
    moreCommunitiesEnabled: Boolean = true
) {
    val containerShape = RoundedCornerShape(percent = 50)
    val tabShape = RoundedCornerShape(percent = 50)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(elevation = 3.dp, shape = containerShape)
            .background(Color(0xFFE7E7E7), containerShape)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 38.dp)
                .background(MaterialTheme.colorScheme.primary, tabShape)
                .semantics { selected = true }
        ) {
            Text(
                text = stringResource(R.string.community_local),
                style = poppinsTextStyle(14, FontWeight.SemiBold),
                color = White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 38.dp)
                .clip(tabShape)
                .background(if (moreCommunitiesEnabled) Color.Transparent else Color(0xFFE7E7E7))
                .clickable(
                    enabled = moreCommunitiesEnabled,
                    role = Role.Tab,
                    onClick = onOtherCommunities
                )
        ) {
            Text(
                text = stringResource(R.string.community_more),
                style = poppinsTextStyle(14, FontWeight.SemiBold),
                color = if (moreCommunitiesEnabled) SageGreen else FadedGray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CommunityHeader(community: Community) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = community.name,
            style = poppinsTextStyle(24, FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        community.locality?.let { locality ->
            Text(
                text = locality,
                style = poppinsTextStyle(13, FontWeight.Light),
                color = Black
            )
        }
    }
}

@Composable
private fun GoalCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = shape)
                .background(White, shape)
                .clip(shape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.community_sample_goal),
                style = interTextStyle(16, FontWeight.Bold),
                color = EcoGreenDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_back),
                contentDescription = null,
                tint = EcoGreenDark,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(-90f)
            )
        }
        Image(
            painter = painterResource(R.drawable.img_peeking),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = (-26).dp)
                .size(width = 88.dp, height = 42.dp)
        )
    }
}

@Composable
private fun CommunityTabs(
    selectedSection: CommunitySection,
    onSectionSelected: (CommunitySection) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        CommunityTab(
            text = stringResource(R.string.community_tab_awards),
            selected = selectedSection == CommunitySection.AWARDS,
            onClick = { onSectionSelected(CommunitySection.AWARDS) },
            modifier = Modifier.weight(1f)
        )
        CommunityTab(
            text = stringResource(R.string.community_tab_events),
            selected = selectedSection == CommunitySection.EVENTS,
            onClick = { onSectionSelected(CommunitySection.EVENTS) },
            modifier = Modifier.weight(1f)
        )
        CommunityTab(
            text = stringResource(R.string.community_tab_news),
            selected = selectedSection == CommunitySection.NEWS,
            onClick = { onSectionSelected(CommunitySection.NEWS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CommunityTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor: Color = if (selected) MossGreen else FadedGray
    val lineColor: Color = if (selected) MaterialTheme.colorScheme.primary else HintGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = text,
                style = interTextStyle(16, FontWeight.Bold),
                color = textColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(lineColor)
        )
    }
}

@Composable
private fun SearchRow(onAction: () -> Unit) {
    val pillShape = RoundedCornerShape(percent = 50)
    val searchLabel = stringResource(R.string.community_search)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(pillShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(role = Role.Button, onClickLabel = searchLabel, onClick = onAction)
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null
            )
            Text(
                text = searchLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            SmallActionPill(
                text = stringResource(R.string.community_create_event),
                onClick = onAction
            )
            SmallActionPill(
                text = stringResource(R.string.community_my_events),
                onClick = onAction
            )
        }
    }
}

@Composable
private fun SmallActionPill(text: String, onClick: () -> Unit) {
    val pillShape = RoundedCornerShape(percent = 50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(96.dp)
            .heightIn(min = 32.dp)
            .clip(pillShape)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Text(
            text = text,
            style = poppinsTextStyle(12),
            color = White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, pillShape)
                .padding(vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityContentPreview() {
    EcoMindTheme {
        CommunityContent(
            state = CommunityUiState(isLoadingEvents = false),
            community = Community(
                id = 1L,
                name = "Lima Community",
                description = "Local community",
                type = pe.greenminds.ecomind.community.domain.valueobjects.CommunityType.LOCAL,
                topic = null,
                locality = "Lima",
                memberLimit = 100,
                iconUrl = null,
                createdBy = null
            )
        )
    }
}
