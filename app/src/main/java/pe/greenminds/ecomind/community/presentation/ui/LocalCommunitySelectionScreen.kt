package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState

@Composable
fun LocalCommunitySelectionScreen(
    state: CommunityUiState,
    onLocalityInputChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onEnter: (Community) -> Unit,
    onJoin: () -> Unit,
    onCancelDetails: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text("Local Community") }
            Button(onClick = {}, enabled = false) { Text("More Communities") }
        }

        if (state.preferredLocality == null) {
            OutlinedTextField(
                value = state.localityInput,
                onValueChange = onLocalityInputChange,
                label = { Text("Your locality") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSearch,
                enabled = state.localityInput.isNotBlank()
            ) {
                Text("Search communities")
            }
        } else {
            Text("Local communities in ${state.preferredLocality}")

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search by name") },
                modifier = Modifier.fillMaxWidth()
            )

            when {
                state.isLoadingCommunity -> CircularProgressIndicator()

                state.errorMessage != null -> {
                    Text(state.errorMessage)
                    TextButton(onClick = onRetry) { Text("Retry") }
                }

                state.filteredLocalCommunities.isEmpty() ->
                    Text("No communities are available in this locality.")

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
            errorMessage = state.errorMessage,
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
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(community.name)
            Text(community.locality.orEmpty())
        }
        Button(onClick = onClick) { Text("Enter") }
    }
}
