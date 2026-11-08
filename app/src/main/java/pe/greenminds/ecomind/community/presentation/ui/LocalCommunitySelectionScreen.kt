package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.White

@Composable
fun LocalCommunitySelectionScreen(
    state: CommunityUiState,
    onLocalityInputChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onEnter: (Community) -> Unit,
    onJoin: () -> Unit,
    onCancelDetails: () -> Unit,
    onRetry: () -> Unit,
    onMoreTab: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CommunitySelector(
            selectedTab = CommunityDirectoryTab.LOCAL,
            onLocalTab = {},
            onMoreTab = onMoreTab
        )

        if (state.preferredLocality == null) {
            OutlinedTextField(
                value = state.localityInput,
                onValueChange = onLocalityInputChange,
                label = { Text(stringResource(R.string.community_locality_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSearch,
                enabled = state.localityInput.isNotBlank()
            ) {
                Text(stringResource(R.string.community_search_communities))
            }
        } else {
            Text(stringResource(R.string.community_local_in_locality, state.preferredLocality))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(stringResource(R.string.community_search_by_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            when {
                state.isLoadingCommunity -> CircularProgressIndicator()

                state.errorMessage != null || state.errorMessageRes != null -> {
                    Text(
                        state.errorMessage
                            ?: state.errorMessageRes?.let { stringResource(it) }.orEmpty()
                    )
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.community_retry)) }
                }

                state.filteredLocalCommunities.isEmpty() ->
                    Text(stringResource(R.string.community_no_local_communities))

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredLocalCommunities, key = { it.id }) { community ->
                        LocalCommunityRow(community, onClick = { onEnter(community) })
                    }
                }
            }
        }
    }

    val pendingCommunity = state.pendingCommunityDetails
    if (pendingCommunity != null) {
        CommunityDetailsDialog(
            community = pendingCommunity,
            isJoining = state.joiningCommunityId == pendingCommunity.id,
            errorMessage = state.errorMessage
                ?: state.errorMessageRes?.let { stringResource(it) },
            onJoin = onJoin,
            onCancel = onCancelDetails
        )
    }
}

@Composable
private fun LocalCommunityRow(
    community: Community,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(White)
            .border(2.dp, CardBorder, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        CommunityIcon(community, modifier = Modifier.size(38.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(community.name)
            community.locality?.takeIf(String::isNotBlank)?.let { locality ->
                Text(locality, fontSize = 10.sp)
            }
        }
        Button(
            onClick = onClick,
            modifier = Modifier.heightIn(min = 32.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 0.dp
            )
        ) {
            Text(stringResource(R.string.community_enter), color = White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
