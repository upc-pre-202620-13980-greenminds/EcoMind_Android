package pe.greenminds.ecomind.notifications.interfaces.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.notifications.domain.model.AppNotification
import pe.greenminds.ecomind.shared.interfaces.components.SectionHeader
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun NotificationsScreen(onBack: () -> Unit, onOpen: (String) -> Unit, onSignIn: () -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    NotificationsContent(state, onBack, { viewModel.markRead(it.id) { onOpen(it.id) } }, viewModel::markAllRead, viewModel::load, onSignIn)
}

@Composable
internal fun NotificationsContent(state: NotificationsUiState, onBack: () -> Unit, onOpen: (AppNotification) -> Unit, onReadAll: () -> Unit, onRetry: () -> Unit, onSignIn: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SectionHeader(stringResource(R.string.notifications_title), onBack)
        if (state.isSimulated) Text(stringResource(R.string.notifications_demo), style = interTextStyle(11), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.sessionRequired -> { Text(stringResource(R.string.achievements_session_required)); TextButton(onSignIn) { Text(stringResource(R.string.achievements_sign_in)) } }
            else -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.notifications_unread, state.unreadCount), Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }, style = interTextStyle(16, FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    TextButton(onReadAll, enabled = state.unreadCount > 0 && !state.isSaving) { Text(stringResource(R.string.notifications_read_all), style = interTextStyle(13, FontWeight.Bold)) }
                }
                if (state.hasError) {
                    Text(stringResource(R.string.notifications_error), color = MaterialTheme.colorScheme.error)
                    TextButton(onRetry) { Text(stringResource(R.string.ranking_try_again)) }
                }
                if (state.items.isEmpty() && !state.hasError) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.notifications_empty), style = interTextStyle(14)) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(state.items, key = { it.id }) { item -> NotificationRow(item, !state.isSaving) { onOpen(item) } }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: AppNotification, enabled: Boolean, onClick: () -> Unit) {
    val content = notificationText(item.id)
    Column {
        Row(Modifier.fillMaxWidth()
            .background(if (item.isRead) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary.copy(alpha = .12f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(content[0]), style = interTextStyle(11, FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text(stringResource(content[1]), style = interTextStyle(14, FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(content[2]), style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.padding(start = 10.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(when { item.ageMinutes < 60 -> stringResource(R.string.notifications_minutes, item.ageMinutes); item.ageMinutes < 1440 -> stringResource(R.string.notifications_hours, item.ageMinutes / 60); item.ageMinutes < 2880 -> stringResource(R.string.notifications_yesterday); else -> stringResource(R.string.notifications_days, item.ageMinutes / 1440) }, style = interTextStyle(11), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!item.isRead) Badge(containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.size(8.dp).semantics { contentDescription = "" })
            }
        }
        if (item.isRead) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private fun notificationText(id: String): List<Int> = when (id) {
    "learning" -> listOf(R.string.notification_learning, R.string.notification_learning_title, R.string.notification_learning_body)
    "quest" -> listOf(R.string.notification_quests, R.string.notification_quest_title, R.string.notification_quest_body)
    "medal" -> listOf(R.string.notification_achievements, R.string.notification_medal_title, R.string.notification_medal_body)
    "reaction" -> listOf(R.string.notification_community, R.string.notification_reaction_title, R.string.notification_reaction_body)
    "shared" -> listOf(R.string.notification_community, R.string.notification_shared_title, R.string.notification_shared_body)
    "event" -> listOf(R.string.notification_events, R.string.notification_event_title, R.string.notification_event_body)
    else -> listOf(R.string.notification_events, R.string.notification_cancelled_title, R.string.notification_cancelled_body)
}
