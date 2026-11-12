package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.EventParticipation
import pe.greenminds.ecomind.community.presentation.viewmodel.CommunityEventsViewModel
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
private val EventGreen = Color(0xFF6BA64E)
private val EventLightGreen = Color(0xFFE4F6D8)
private val EventRed = Color(0xFFE35B63)
private val EventTeal = Color(0xFF329FA2)

@Composable
internal fun CommunityEventsContent(
    viewModel: CommunityEventsViewModel,
    canCreate: Boolean,
    onCreate: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val errorText = state.error
        ?: state.errorRes?.let { stringResource(it) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.search,
                onValueChange = viewModel::search,
                placeholder = { Text(stringResource(R.string.event_flow_search)) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f)
            )

            Column {
                if (canCreate) {
                    EventFlowButton(
                        text = stringResource(R.string.event_flow_create),
                        onClick = onCreate,
                        enabled = !state.busy
                    )
                }

                EventFlowButton(
                    text = stringResource(
                        if (state.mine) R.string.event_flow_events
                        else R.string.event_flow_mine
                    ),
                    onClick = viewModel::toggleMine,
                    enabled = !state.busy
                )
            }
        }

        errorText?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = viewModel::refresh, enabled = !state.busy) {
                Text(stringResource(R.string.event_flow_retry))
            }
        }

        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (!state.loading && state.visibleItems.isEmpty()) {
            Text(stringResource(R.string.event_flow_empty))
        }

        state.visibleItems.forEach { item ->
            EventParticipationCard(
                item = item,
                mine = state.mine,
                enabled = !state.busy,
                onJoin = { viewModel.openJoin(item) },
                onChat = { viewModel.openChat(item) },
                onLeave = { viewModel.requestLeave(item) }
            )
        }
    }

    state.joinTarget?.let { item ->
        val joinActive = rememberEventActive(item.chatExpiresAt)
        Dialog(onDismissRequest = viewModel::dismissJoin) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EventLightGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.event.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(
                                R.string.event_flow_limit,
                                item.participantCount,
                                item.totalCapacity
                            )
                        )
                        Text(stringResource(R.string.event_flow_creator, item.creatorName))
                        Text(item.event.description.orEmpty())
                        Text(stringResource(R.string.event_flow_date, displayDate(item.event.date)))
                        Text(
                            stringResource(
                                R.string.event_flow_time,
                                displayTime(item.event.startTime)
                            )
                        )
                        Text(
                            stringResource(
                                R.string.event_flow_address,
                                item.event.location.orEmpty()
                            )
                        )
                    }

                    errorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (state.busy) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                    }

                    EventFlowButton(
                        text = stringResource(R.string.event_flow_individual),
                        onClick = { viewModel.join(false) },
                        enabled = !state.busy && joinActive,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    if (item.familyId != null) {
                        EventFlowButton(
                            text = stringResource(R.string.event_flow_family),
                            onClick = { viewModel.join(true) },
                            enabled = !state.busy && joinActive,
                            color = EventTeal
                        )
                    }

                    TextButton(
                        onClick = viewModel::dismissJoin,
                        enabled = !state.busy
                    ) {
                        Text(stringResource(R.string.event_flow_cancel))
                    }
                }
            }
        }
    }

    state.joinedAsFamily?.let { family ->
        Dialog(onDismissRequest = viewModel::dismissJoined) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (family) R.string.event_flow_success_family
                            else R.string.event_flow_success_individual
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Image(
                        painter = painterResource(
                            if (family) R.drawable.join_family
                            else R.drawable.join_individually
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(150.dp)
                    )

                    EventFlowButton(
                        text = stringResource(R.string.event_flow_continue),
                        onClick = viewModel::dismissJoined
                    )
                }
            }
        }
    }

    state.leaveTarget?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::dismissLeave,
            title = { Text(stringResource(R.string.event_flow_leave_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.event.title)
                    Text(stringResource(R.string.event_flow_leave_question))
                    errorText?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    if (state.busy) CircularProgressIndicator()
                }
            },
            confirmButton = {
                Column {
                    if (item.joined) {
                        TextButton(
                            onClick = { viewModel.leave(false) },
                            enabled = !state.busy
                        ) {
                            Text(stringResource(R.string.event_flow_leave_self))
                        }
                    }
                    if (item.canCancelGroup) {
                        TextButton(
                            onClick = { viewModel.leave(true) },
                            enabled = !state.busy
                        ) {
                            Text(stringResource(R.string.event_flow_leave_group))
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissLeave,
                    enabled = !state.busy
                ) {
                    Text(stringResource(R.string.event_flow_cancel))
                }
            }
        )
    }
}

@Composable
private fun EventParticipationCard(
    item: EventParticipation,
    mine: Boolean,
    enabled: Boolean,
    onJoin: () -> Unit,
    onChat: () -> Unit,
    onLeave: () -> Unit
) {
    val active = rememberEventActive(item.chatExpiresAt)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, Color(0xFFDADADA), RoundedCornerShape(32.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = item.event.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (mine) EventImage(item)

            Column(modifier = Modifier.weight(1f)) {
                if (!mine) {
                    Text(
                        item.event.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    stringResource(
                        R.string.event_flow_participants,
                        item.participantCount,
                        item.totalCapacity
                    ),
                    style = MaterialTheme.typography.bodySmall
                )

                if (mine) {
                    Text(
                        item.communityName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!mine) {
                EventImage(item)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(
                            if (active) R.string.event_flow_active
                            else R.string.event_flow_inactive
                        ),
                        color = EventGreen,
                        fontWeight = FontWeight.Bold
                    )

                    EventFlowButton(
                        text = stringResource(R.string.event_flow_chat),
                        onClick = onChat,
                        enabled = enabled && active && item.chatAvailable
                    )

                    EventFlowButton(
                        text = stringResource(R.string.event_flow_leave),
                        onClick = onLeave,
                        enabled = enabled && !item.creator &&
                                (item.joined || item.canCancelGroup),
                        color = EventRed
                    )
                }
            }
        }

        if (!mine) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventFlowButton(
                    text = stringResource(
                        when {
                            item.joined -> R.string.event_flow_joined
                            item.canCancelGroup -> R.string.event_flow_managing
                            else -> R.string.event_flow_join
                        }
                    ),
                    onClick = onJoin,
                    enabled = enabled && active && !item.joined &&
                            !item.canCancelGroup && (item.event.capacity ?: 0) > 0,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onLeave,
                    enabled = enabled && !item.creator &&
                            (item.joined || item.canCancelGroup)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.event_flow_leave),
                        tint = if (enabled && !item.creator &&
                            (item.joined || item.canCancelGroup)
                        ) EventRed else Color.Gray
                    )
                }
            }
        }

        if (mine && item.creator) {
            Text(
                stringResource(R.string.event_flow_creator_leave),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EventImage(item: EventParticipation) {
    AsyncImage(
        model = item.event.imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = 97.dp, height = 65.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(EventLightGreen)
    )
}

@Composable
internal fun EventFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = EventGreen
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        modifier = modifier.heightIn(min = 40.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

private fun displayDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}.getOrDefault(value)

private fun displayTime(value: String?): String = runCatching {
    LocalTime.parse(value).format(DateTimeFormatter.ofPattern("hh:mm a"))
}.getOrDefault(value.orEmpty())

@Composable
private fun rememberEventActive(expiresAt: String): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current

    val expiration = remember(expiresAt) {
        runCatching { Instant.parse(expiresAt) }.getOrNull()
    }

    val active by produceState(
        initialValue = expiration?.let { Instant.now().isBefore(it) } ?: false,
        key1 = expiration,
        key2 = lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            do {
                val stillActive = expiration?.let {
                    Instant.now().isBefore(it)
                } ?: false

                value = stillActive

                if (!stillActive) {
                    break
                }

                delay(1000)
            } while (true)
        }
    }

    return active
}