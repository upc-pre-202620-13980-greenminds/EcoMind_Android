package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState
import pe.greenminds.ecomind.community.presentation.states.TopicCommunityListMode
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White

@Composable
fun MoreCommunitiesScreen(
    state: CommunityUiState,
    canCreateCommunity: Boolean,
    onLocalTab: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleList: () -> Unit,
    onCreateCommunity: () -> Unit,
    onOpenDetails: (Community) -> Unit,
    onConfirmJoin: () -> Unit,
    onCancelDetails: () -> Unit,
    onEnterCommunity: (Community) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        CommunitySelector(
            selectedTab = CommunityDirectoryTab.MORE,
            moreEnabled = state.canAccessMoreCommunities,
            onLocalTab = onLocalTab,
            onMoreTab = {}
        )

        MoreCommunitiesSearchRow(
            query = state.searchQuery,
            canCreateCommunity = canCreateCommunity,
            onQueryChange = onSearchQueryChange,
            createLabel = stringResource(R.string.community_create),
            toggleLabel = stringResource(
                when (state.topicListMode) {
                    TopicCommunityListMode.MEMBERS -> R.string.community_search
                    TopicCommunityListMode.DISCOVER -> R.string.community_communities
                }
            ),
            onCreate = onCreateCommunity,
            onToggleList = onToggleList,
        )

        when {
            state.isLoadingTopicCommunities -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                )
            }

            state.errorMessage != null || state.errorMessageRes != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage
                            ?: state.errorMessageRes?.let { stringResource(it) }.orEmpty()
                    )

                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.community_retry))
                    }
                }
            }

            state.visibleTopicCommunities.isEmpty() &&
                    state.topicListMode == TopicCommunityListMode.MEMBERS -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.community_no_joined_topics),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.community_join_one),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            state.visibleTopicCommunities.isEmpty() -> {
                Text(
                    text = stringResource(R.string.community_no_topic_results),
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.visibleTopicCommunities,
                        key = { community -> community.id }
                    ) { community ->
                        val isMember =
                            state.topicListMode == TopicCommunityListMode.MEMBERS

                        TopicCommunityRow(
                            community = community,
                            actionLabel = if (isMember) {
                                stringResource(R.string.community_enter)
                            } else {
                                stringResource(R.string.community_join)
                            },
                            onClick = {
                                if (isMember) {
                                    onEnterCommunity(community)
                                } else {
                                    onOpenDetails(community)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    state.pendingCommunityDetails?.let { community ->
        CommunityDetailsDialog(
            community = community,
            isJoining = state.joiningCommunityId == community.id,
            errorMessage = state.errorMessage
                ?: state.errorMessageRes?.let { stringResource(it) },
            onJoin = onConfirmJoin,
            onCancel = onCancelDetails
        )
    }
}

@Composable
private fun MoreCommunitiesSearchRow(
    query: String,
    canCreateCommunity: Boolean,
    onQueryChange: (String) -> Unit,
    createLabel: String,
    toggleLabel: String,
    onCreate: () -> Unit,
    onToggleList: () -> Unit
) {
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
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = searchLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            if (canCreateCommunity) {
                SmallActionPill(
                    text = createLabel,
                    onClick = onCreate
                )
            }
            SmallActionPill(
                text = toggleLabel,
                onClick = onToggleList
            )
        }
    }
}

@Composable
private fun TopicCommunityRow(
    community: Community,
    actionLabel: String,
    onClick: () -> Unit
) {
    val rowShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clip(rowShape)
            .background(White)
            .border(
                width = 2.dp,
                color = CardBorder,
                shape = rowShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommunityIcon(
            community = community,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = community.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )

        Button(
            onClick = onClick,
            modifier = Modifier.heightIn(min = 32.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGreen
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 0.dp
            )
        ) {
            Text(
                text = actionLabel,
                color = White,
                fontSize = 12.sp
            )
        }
    }
}
