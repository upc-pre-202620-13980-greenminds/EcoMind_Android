package pe.greenminds.ecomind.community.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.community.presentation.states.CommunityEventsUiState
import pe.greenminds.ecomind.community.presentation.viewmodel.CommunityEventsViewModel

@Composable
internal fun EventChatScreen(
    state: CommunityEventsUiState,
    viewModel: CommunityEventsViewModel,
    selectedTab: CommunityDirectoryTab,
    moreEnabled: Boolean,
    onBack: () -> Unit,
    onLocalTab: () -> Unit,
    onMoreTab: () -> Unit
) {
    val item = state.chat ?: return
    val chatErrorText = state.chatError ?: state.chatErrorRes?.let { stringResource(it) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(item.event.id, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.pollChat()
        }
    }

    LaunchedEffect(state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            CommunitySelector(
                selectedTab = selectedTab,
                moreEnabled = moreEnabled,
                onLocalTab = onLocalTab,
                onMoreTab = onMoreTab
            )

            Text(
                text = item.communityName,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6BA64E),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = stringResource(R.string.event_flow_events),
                color = Color(0xFF6BA64E),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        HorizontalDivider(
            color = Color(0xFF6BA64E),
            thickness = 2.dp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_back),
                    contentDescription = stringResource(R.string.event_flow_back)
                )
            }
            Text(
                text = item.event.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6BA64E)
            )
        }

        if (state.chatLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        chatErrorText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (state.chatClosed) {
            Text(
                text = stringResource(R.string.event_flow_unavailable),
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (
                state.messages.isEmpty() &&
                !state.chatLoading &&
                !state.chatClosed
            ) {
                item {
                    Text(stringResource(R.string.event_flow_no_messages))
                }
            }

            items(state.messages, key = { it.id }) { message ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (message.isMine) {
                        Alignment.End
                    } else {
                        Alignment.Start
                    }
                ) {
                    Text(
                        text = if (message.senderId == state.chatAuthorId) {
                            stringResource(R.string.event_flow_admin, message.senderName)
                        } else {
                            message.senderName
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6BA64E)
                    )

                    Text(
                        text = message.text,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(
                                1.dp,
                                Color(0xFF93C77D),
                                RoundedCornerShape(18.dp)
                            )
                            .background(
                                Color(0xFFE4F6D8),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::changeDraft,
                enabled = !state.chatClosed && !state.sending,
                placeholder = { Text(stringResource(R.string.event_flow_message)) },
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )

            EventFlowButton(
                text = stringResource(R.string.event_flow_send),
                onClick = viewModel::send,
                enabled = !state.chatClosed && !state.sending &&
                        state.draft.isNotBlank()
            )
        }
    }
}