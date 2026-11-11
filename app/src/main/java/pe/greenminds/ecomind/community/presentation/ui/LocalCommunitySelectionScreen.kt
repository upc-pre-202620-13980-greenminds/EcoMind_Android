package pe.greenminds.ecomind.community.presentation.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun LocalCommunitySelectionScreen(
    state: CommunityUiState,
    onLocalityInputChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSaveLocality: () -> Unit,
    onSearch: () -> Unit,
    onEnter: (Community) -> Unit,
    onJoin: () -> Unit,
    onCancelDetails: () -> Unit,
    onRetry: () -> Unit,
    onMoreTab: () -> Unit,
    onLocationRequested: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onDetectLocality: () -> Unit,
    onCancelLocation: () -> Unit
) {
    val context = LocalContext.current
    var showLocalityDialog by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onDetectLocality()
        } else {
            onLocationPermissionDenied()
        }
    }

    val requestLocation: () -> Unit = {
        onLocationRequested()

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onDetectLocality()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LaunchedEffect(
        state.preferredLocality,
        state.hasRequestedLocation,
        state.errorMessage,
        state.errorMessageRes
    ) {
        if (state.preferredLocality == null &&
            !state.hasRequestedLocation &&
            state.errorMessage == null &&
            state.errorMessageRes == null
        ) {
            requestLocation()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        onCancelLocation()
    }

    DisposableEffect(Unit) {
        onDispose {
            onCancelLocation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CommunitySelector(
            selectedTab = CommunityDirectoryTab.LOCAL,
            moreEnabled = state.canAccessMoreCommunities,
            onLocalTab = {},
            onMoreTab = onMoreTab
        )

        LocalCommunitySearchRow(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            enabled = state.preferredLocality != null &&
                    !state.isResolvingLocality &&
                    !state.isLoadingLocalCommunities
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = White,
            shadowElevation = 5.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(
                            R.string.community_locality_value,
                            state.preferredLocality
                                ?: stringResource(R.string.community_locality_not_selected)
                        ),
                        style = interTextStyle(16, FontWeight.Bold),
                        color = TextSecondary
                    )

                    TextButton(
                        onClick = {
                            onLocalityInputChange(state.preferredLocality.orEmpty())
                            showLocalityDialog = true
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.community_change_locality),
                            textDecoration = TextDecoration.Underline,
                            color = EcoGreen
                        )
                    }
                }

                state.localityErrorRes?.let { message ->
                    Text(
                        text = stringResource(message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                when {
                    state.isResolvingLocality -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(stringResource(R.string.community_detecting_locality))
                    }

                    state.isLoadingLocalCommunities -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    state.errorMessage != null || state.errorMessageRes != null -> {
                        Text(
                            text = state.errorMessage
                                ?: state.errorMessageRes
                                    ?.let { stringResource(it) }
                                    .orEmpty()
                        )
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.community_retry))
                        }
                    }

                    state.preferredLocality == null -> {
                        Text(stringResource(R.string.community_choose_locality))
                        TextButton(onClick = requestLocation) {
                            Text(stringResource(R.string.community_use_my_location))
                        }
                    }

                    state.filteredLocalCommunities.isEmpty() -> {
                        Text(
                            stringResource(
                                if (state.searchQuery.isBlank()) {
                                    R.string.community_no_local_communities
                                } else {
                                    R.string.community_no_name_results
                                }
                            )
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(
                                items = state.filteredLocalCommunities,
                                key = { it.id }
                            ) { community ->
                                LocalCommunityRow(
                                    community = community,
                                    onClick = { onEnter(community) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocalityDialog) {
        AlertDialog(
            onDismissRequest = { showLocalityDialog = false },
            title = {
                Text(stringResource(R.string.community_change_locality))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.community_location_explanation))

                    OutlinedTextField(
                        value = state.localityInput,
                        onValueChange = onLocalityInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(stringResource(R.string.community_locality_label))
                        }
                    )

                    TextButton(
                        onClick = {
                            showLocalityDialog = false
                            requestLocation()
                        }
                    ) {
                        Text(stringResource(R.string.community_use_my_location))
                    }

                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    ) {
                        Text(stringResource(R.string.community_location_settings))
                    }

                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    ) {
                        Text(stringResource(R.string.community_permission_settings))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = state.localityInput.isNotBlank(),
                    onClick = {
                        onSaveLocality()
                        showLocalityDialog = false
                    }
                ) {
                    Text(stringResource(R.string.community_apply_locality))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocalityDialog = false }) {
                    Text(stringResource(R.string.community_cancel))
                }
            }
        )
    }

    state.pendingCommunityDetails?.let { community ->
        CommunityDetailsDialog(
            community = community,
            isJoining = state.joiningCommunityId == community.id,
            errorMessage = state.errorMessage
                ?: state.errorMessageRes?.let { stringResource(it) },
            onJoin = onJoin,
            onCancel = onCancelDetails
        )
    }
}

@Composable
private fun LocalCommunitySearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                singleLine = true,
                textStyle = interTextStyle(16).copy(color = TextSecondary),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.community_search),
                                style = interTextStyle(16),
                                color = TextSecondary
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSearch,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Text(stringResource(R.string.community_search))
        }
    }
}

@Composable
private fun LocalCommunityRow(
    community: Community,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .border(4.dp, CardBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommunityIcon(
            community = community,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = community.name,
            modifier = Modifier.weight(1f),
            style = interTextStyle(17, FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.community_join),
                style = interTextStyle(13),
                color = White
            )
        }
    }
}