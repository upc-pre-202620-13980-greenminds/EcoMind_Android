package pe.greenminds.ecomind.notifications.interfaces

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.preferences.PreferencesHeader
import pe.greenminds.ecomind.shared.interfaces.preferences.RequestNotificationPermission
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MINUTES = 60L
private const val DAY_MINUTES = 24 * HOUR_MINUTES

// Frames 63, 64, 66 and 67, the same for students and parents in this app
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpen: (NotificationDestination) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.openDestination) {
        state.openDestination?.let { destination ->
            viewModel.onDestinationOpened()
            onOpen(destination)
        }
    }
    RequestNotificationPermission()

    NotificationsContent(
        state = state,
        nowMillis = System.currentTimeMillis(),
        onNotificationClick = viewModel::onNotificationClick,
        onMarkAllAsRead = viewModel::markAllAsRead,
        onRetry = viewModel::loadNotifications,
        onBack = onBack
    )
}

@Composable
private fun NotificationsContent(
    state: NotificationsUiState,
    nowMillis: Long,
    onNotificationClick: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        PreferencesHeader(title = stringResource(R.string.notifications_title), onBack = onBack)

        when {
            state.isLoading && state.notifications.isEmpty() -> {
                val loadingDescription = stringResource(R.string.loading)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { contentDescription = loadingDescription }
                    )
                }
            }

            state.loadFailed -> LoadFailure(onRetry = onRetry)

            state.notifications.isEmpty() -> CenteredMessage(
                title = stringResource(R.string.notifications_empty_title),
                message = stringResource(R.string.notifications_empty_message),
                modifier = Modifier.fillMaxSize()
            )

            else -> {
                InboxHeader(
                    unreadCount = state.unreadCount,
                    isMarkingAll = state.isMarkingAll,
                    onMarkAllAsRead = onMarkAllAsRead
                )
                if (state.actionError != null) {
                    Text(
                        text = stringResource(state.actionError),
                        style = interTextStyle(13),
                        color = EcoMindTheme.colors.errorRed,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            time = relativeTime(notification.createdAtMillis, nowMillis),
                            onClick = { onNotificationClick(notification.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// "3 unread" and "Mark all as read" (frame 63), or "All caught up" with the action off (frame 64)
@Composable
private fun InboxHeader(
    unreadCount: Int,
    isMarkingAll: Boolean,
    onMarkAllAsRead: () -> Unit
) {
    val canMarkAll = unreadCount > 0 && !isMarkingAll

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = if (unreadCount > 0) {
                pluralStringResource(R.plurals.notifications_unread, unreadCount, unreadCount)
            } else {
                stringResource(R.string.notifications_all_read)
            },
            style = interTextStyle(16, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                }
        )
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(enabled = canMarkAll, role = Role.Button, onClick = onMarkAllAsRead)
        ) {
            Text(
                text = stringResource(R.string.notifications_mark_all),
                style = interTextStyle(13, FontWeight.Bold),
                color = if (canMarkAll) {
                    MaterialTheme.colorScheme.primary
                } else {
                    EcoMindTheme.colors.placeholder
                }
            )
        }
    }
}

// An unread notification is a light green card with a dot; a read one is a row with a divider
@Composable
private fun NotificationRow(
    notification: Notification,
    time: String,
    onClick: () -> Unit
) {
    val category = stringResource(categoryLabel(notification.category))
    val description = stringResource(
        if (notification.isRead) {
            R.string.notifications_row_read_description
        } else {
            R.string.notifications_row_unread_description
        },
        category,
        notification.title,
        notification.message,
        time
    )
    val shape = RoundedCornerShape(12.dp)

    Column {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .then(
                    if (notification.isRead) {
                        Modifier
                    } else {
                        Modifier
                            .clip(shape)
                            .background(EcoGreen.copy(alpha = 0.12f))
                    }
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                // The whole row is read as one sentence: state, category, title, text and time
                .clearAndSetSemantics {
                    contentDescription = description
                    role = Role.Button
                }
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category,
                    style = interTextStyle(11, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = notification.title,
                    style = interTextStyle(14, FontWeight.Bold),
                    color = EcoMindTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                    text = notification.message,
                    style = interTextStyle(12),
                    color = EcoMindTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = time,
                    style = interTextStyle(11),
                    color = EcoMindTheme.colors.textSecondary
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
        if (notification.isRead) {
            HorizontalDivider(thickness = 1.dp, color = EcoMindTheme.colors.rowDivider)
        }
    }
}

// Frame 67
@Composable
private fun LoadFailure(onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        CenteredMessage(
            title = stringResource(R.string.notifications_error_title),
            message = stringResource(R.string.notifications_error_message),
            modifier = Modifier.weight(1f)
        )
        PillButton(
            text = stringResource(R.string.notifications_try_again),
            onClick = onRetry,
            faceColor = EcoGreen,
            baseColor = EcoGreenDark,
            height = 48.dp,
            textStyle = interTextStyle(16, FontWeight.Bold),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 40.dp)
        )
    }
}

// Frames 66 and 67: green title and gray text in the middle of the screen
@Composable
private fun CenteredMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            text = title,
            style = interTextStyle(18, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = message,
            style = interTextStyle(13),
            color = EcoMindTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
        )
    }
}

// "5 min", "1 h", "Yesterday" or "2 days", as in frame 63
@Composable
private fun relativeTime(createdAtMillis: Long, nowMillis: Long): String {
    val minutes = ((nowMillis - createdAtMillis) / MINUTE_MILLIS).coerceAtLeast(0)
    return when {
        minutes < HOUR_MINUTES ->
            stringResource(R.string.notifications_time_minutes, minutes.coerceAtLeast(1))
        minutes < DAY_MINUTES ->
            stringResource(R.string.notifications_time_hours, minutes / HOUR_MINUTES)
        minutes < 2 * DAY_MINUTES -> stringResource(R.string.notifications_time_yesterday)
        else -> {
            val days = (minutes / DAY_MINUTES).toInt()
            pluralStringResource(R.plurals.notifications_time_days, days, days)
        }
    }
}

private fun categoryLabel(category: NotificationCategory): Int = when (category) {
    NotificationCategory.NEW_AND_DAILY_QUESTS -> R.string.notifications_label_quests
    NotificationCategory.LEARNING_REMINDERS -> R.string.notifications_label_learning
    NotificationCategory.ACHIEVEMENTS_AND_BADGES -> R.string.notifications_label_achievements
    NotificationCategory.WEEKLY_PROGRESS_SUMMARY -> R.string.notifications_label_progress
    NotificationCategory.COMMUNITY_EVENTS -> R.string.notifications_label_events
    NotificationCategory.COMMUNITY_ACTIVITY -> R.string.notifications_label_community
}

private val previewNow = 1_800_000_000_000L

private val previewNotifications = listOf(
    Notification(1L, NotificationCategory.LEARNING_REMINDERS, "EcoMind", "EcoMind", false,
        previewNow - 5 * MINUTE_MILLIS),
    Notification(2L, NotificationCategory.NEW_AND_DAILY_QUESTS, "EcoMind", "EcoMind", false,
        previewNow - 60 * MINUTE_MILLIS),
    Notification(3L, NotificationCategory.COMMUNITY_ACTIVITY, "EcoMind", "EcoMind", true,
        previewNow - 30 * HOUR_MINUTES * MINUTE_MILLIS),
    Notification(4L, NotificationCategory.COMMUNITY_EVENTS, "EcoMind", "EcoMind", true,
        previewNow - 2 * DAY_MINUTES * MINUTE_MILLIS)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun NotificationsPreview() {
    EcoMindTheme {
        NotificationsContent(
            state = NotificationsUiState(isLoading = false, notifications = previewNotifications),
            nowMillis = previewNow,
            onNotificationClick = {},
            onMarkAllAsRead = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun NotificationsEmptyPreview() {
    EcoMindTheme {
        NotificationsContent(
            state = NotificationsUiState(isLoading = false),
            nowMillis = previewNow,
            onNotificationClick = {},
            onMarkAllAsRead = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun NotificationsFailurePreview() {
    EcoMindTheme {
        NotificationsContent(
            state = NotificationsUiState(isLoading = false, loadFailed = true),
            nowMillis = previewNow,
            onNotificationClick = {},
            onMarkAllAsRead = {},
            onRetry = {},
            onBack = {}
        )
    }
}
