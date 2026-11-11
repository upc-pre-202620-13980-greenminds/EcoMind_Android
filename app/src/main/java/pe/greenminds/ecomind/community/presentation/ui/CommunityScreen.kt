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
import androidx.compose.material3.IconButton
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
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
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
import pe.greenminds.ecomind.community.presentation.states.TopicCommunityListMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

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
        state.isShowingMoreCommunitiesList -> {
            MoreCommunitiesScreen(
                state = state,
                onLocalTab = viewModel::showLocalCommunity,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onToggleList = {
                    when (state.topicListMode) {
                        TopicCommunityListMode.MEMBERS ->
                            viewModel.showTopicDiscovery()

                        TopicCommunityListMode.DISCOVER ->
                            viewModel.showMyTopicCommunities()
                    }
                },
                onCreateCommunity = {},
                onOpenDetails = viewModel::showCommunityDetails,
                onConfirmJoin = viewModel::joinPendingCommunity,
                onCancelDetails = viewModel::dismissCommunityDetails,
                onEnterCommunity = viewModel::enterTopicCommunity,
                onRetry = viewModel::retry
            )
        }
        state.selectedCommunity == null -> {
            LocalCommunitySelectionScreen(
                state = state,
                onLocalityInputChange = viewModel::updateLocalityInput,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSaveLocality = viewModel::saveLocalityAndSearch,
                onSearch = viewModel::searchCurrentLocality,
                onEnter = viewModel::showCommunityDetails,
                onJoin = viewModel::joinPendingCommunity,
                onCancelDetails = viewModel::dismissCommunityDetails,
                onRetry = viewModel::retry,
                onMoreTab = viewModel::showMoreCommunities,
                onLocationRequested = viewModel::markLocationRequested,
                onLocationPermissionDenied = viewModel::onLocationPermissionDenied,
                onDetectLocality = viewModel::detectCurrentLocality,
                onCancelLocation = viewModel::cancelLocalityDetection
            )
        }

        else -> {
            CommunityContent(
                state = state,
                community = checkNotNull(state.selectedCommunity),
                onSectionSelected = viewModel::selectSection,
                onLocalTab = viewModel::showLocalCommunity,
                onMoreTab = viewModel::showMoreCommunities,
                onBackToTopicCommunities = viewModel::showMoreCommunities,
                onCommunityOptions = viewModel::toggleCommunityOptions,
                onDismissCommunityOptions = viewModel::dismissCommunityOptions,
                onLeaveCommunity = viewModel::requestLeaveCommunity,
                onConfirmLeave = viewModel::confirmLeaveCommunity,
                onCancelLeave = viewModel::dismissLeaveConfirmation
            )
        }
    }
}

@Composable
private fun CommunityContent(
    state: CommunityUiState,
    community: Community,
    onSectionSelected: (CommunitySection) -> Unit,
    onLocalTab: () -> Unit,
    onMoreTab: () -> Unit,
    onBackToTopicCommunities: () -> Unit,
    modifier: Modifier = Modifier,
    onCommunityOptions: () -> Unit,
    onDismissCommunityOptions: () -> Unit,
    onLeaveCommunity: () -> Unit,
    onConfirmLeave: () -> Unit,
    onCancelLeave: () -> Unit
)  {
    var showComingSoon by rememberSaveable { mutableStateOf(false) }
    val openComingSoon = { showComingSoon = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CommunitySelector(
                selectedTab = state.activeDirectoryTab,
                onLocalTab = onLocalTab,
                onMoreTab = onMoreTab,
                moreEnabled = state.canAccessMoreCommunities,
            )
            if (community.type == CommunityType.TOPIC) {
                IconButton(onClick = onBackToTopicCommunities) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_back),
                        contentDescription = stringResource(R.string.community_back_to_topics)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CommunityHeader(community)
                }

                Box {
                    IconButton(
                        onClick = onCommunityOptions,
                        enabled = !state.isLeavingCommunity
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.community_options)
                        )
                    }

                    DropdownMenu(
                        expanded = state.showCommunityOptions,
                        onDismissRequest = onDismissCommunityOptions
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.community_leave))
                            },
                            onClick = onLeaveCommunity
                        )
                    }
                }
            }
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
    if (showComingSoon) {
        ComingSoonDialog(onDismiss = { showComingSoon = false })
    }

    if (state.showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelLeave,
            title = {
                Text(stringResource(R.string.community_leave_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.community_leave_confirmation,
                        community.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmLeave,
                    enabled = !state.isLeavingCommunity
                ) {
                    if (state.isLeavingCommunity) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.community_leave))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelLeave,
                    enabled = !state.isLeavingCommunity
                ) {
                    Text(stringResource(R.string.community_cancel))
                }
            }
        )
    }
}

@Composable
internal fun CommunitySelector(
    selectedTab: CommunityDirectoryTab,
    moreEnabled: Boolean,
    onLocalTab: () -> Unit,
    onMoreTab: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    val localSelected = selectedTab == CommunityDirectoryTab.LOCAL
    val moreSelected = selectedTab == CommunityDirectoryTab.MORE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(5.dp, shape)
            .background(White, shape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp)
                .clip(shape)
                .background(
                    if (localSelected) MaterialTheme.colorScheme.primary
                    else Color(0xFFB3B3B3)
                )
                .clickable(role = Role.Tab, onClick = onLocalTab)
                .semantics { selected = localSelected },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.community_local),
                style = interTextStyle(14, FontWeight.Bold),
                color = White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp)
                .clip(shape)
                .background(
                    if (moreSelected && moreEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color(0xFFB3B3B3)
                    }
                )
                .clickable(
                    enabled = moreEnabled,
                    role = Role.Tab,
                    onClick = onMoreTab
                )
                .semantics { selected = moreSelected },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.community_more),
                style = interTextStyle(14, FontWeight.Bold),
                color = White,
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
internal fun SmallActionPill(text: String, onClick: () -> Unit) {
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
            ),
            onSectionSelected = {},
            onLocalTab = {},
            onMoreTab = {},
            onBackToTopicCommunities = {},
            onCommunityOptions = {},
            onDismissCommunityOptions = {},
            onLeaveCommunity = {},
            onConfirmLeave = {},
            onCancelLeave = {}
        )
    }
}
