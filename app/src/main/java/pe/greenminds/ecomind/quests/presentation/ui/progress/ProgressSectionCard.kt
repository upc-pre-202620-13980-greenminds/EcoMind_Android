package pe.greenminds.ecomind.quests.presentation.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.FieldGray
import pe.greenminds.ecomind.shared.interfaces.theme.PaleGreen
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// White card with a green label over its top edge, a row per quest and an optional action
@Composable
fun ProgressSectionCard(
    label: String,
    entries: List<ProgressEntry>,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onOpenEntry: ((ProgressEntry) -> Unit)? = null,
    emptyText: String? = null,
    pendingAssignmentIds: Set<Long> = emptySet(),
    contentAfterEntries: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {}
) {
    val cardShape = RoundedCornerShape(18.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Leaves room above for the half of the label that sits outside the card
                .padding(top = 8.dp)
                .shadow(elevation = 3.dp, shape = cardShape)
                .background(White, cardShape)
                .padding(start = 14.dp, end = 14.dp, top = 44.dp, bottom = 12.dp)
        ) {
            entries.forEach { entry ->
                key(entry.questId) {
                    ProgressRow(entry, pending = entry.questUser.id in pendingAssignmentIds, onOpen = onOpenEntry?.let { { it(entry) } })
                }
            }
            if (entries.isEmpty() && emptyText != null) {
                Text(emptyText, style = interTextStyle(15), color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
            }

            contentAfterEntries()

            if (actionLabel != null) {
                // The chip is small as in the design; the box keeps a touch target of 48dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.End)
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(role = Role.Button, onClick = onAction)
                ) {
                    Text(
                        text = actionLabel,
                        style = interTextStyle(14, FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(PaleGreen, RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        SectionLabel(text = label)
    }
}

// Looks like the rounded buttons of the app, but it is only a title
@Composable
private fun SectionLabel(text: String) {
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = Modifier
            .widthIn(min = 155.dp)
            .height(39.dp)
            .shadow(elevation = 3.dp, shape = shape)
            .semantics { heading() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 4.dp)
                .background(EcoGreenDark, shape)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 4.dp)
                .background(EcoGreen, shape)
        ) {
            Text(
                text = text,
                style = interTextStyle(16, FontWeight.Bold),
                color = White,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
internal fun ProgressRow(entry: ProgressEntry, pending: Boolean = false, onOpen: (() -> Unit)? = null) {
    var expanded by rememberSaveable(entry.questId) { mutableStateOf(false) }
    // The bar has no number on screen, so the screen reader gets the percentage as text
    val title = entry.title.ifBlank { stringResource(R.string.progress_unknown_quest) }
    val description = stringResource(R.string.progress_entry_description, title, entry.percent)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .then(if (onOpen != null) Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier)
                .semantics(mergeDescendants = true) { contentDescription = description },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = interTextStyle(15, FontWeight.Medium),
                    color = TextPrimary
                )
                if (pending) {
                    Text(
                        stringResource(R.string.offline_quest_pending_short),
                        style = interTextStyle(13),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (entry.questUser.status == QuestStatus.COMPLETED) {
                    entry.questUser.endDate?.let { date ->
                        Text(
                            text = stringResource(R.string.progress_completed_on, progressDate(date)),
                            style = interTextStyle(13),
                            color = TextPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            if (entry.otherCompletions.isNotEmpty()) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = stringResource(if (expanded) R.string.progress_hide_completions else R.string.progress_show_completions),
                        tint = TextPrimary,
                        modifier = Modifier.rotate(if (expanded) 180f else 0f)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onOpen != null) Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier)
                .padding(top = 8.dp)
                .height(12.dp)
                .background(FieldGray, RoundedCornerShape(percent = 50))
        ) {
            Box(
                modifier = Modifier
                    // Part of the track that is filled, from 0 to 1
                    .fillMaxWidth(fraction = entry.percent / 100f)
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50))
            )
        }
        if (expanded && entry.otherCompletions.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp)) {
                entry.otherCompletions.forEach { completion ->
                    Text(
                        text = completion.endDate?.let { progressDate(it) }
                            ?: stringResource(R.string.progress_completion_date_unknown),
                        style = interTextStyle(14),
                        color = TextPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun progressDate(date: String): String {
    val locale = LocalConfiguration.current.locales[0]
    return try {
        LocalDate.parse(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    } catch (_: Exception) { date }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProgressSectionCardPreview() {
    EcoMindTheme {
        ProgressSectionCard(
            label = stringResource(R.string.progress_in_progress),
            entries = listOf(
                previewProgressEntry(1L, stringResource(R.string.app_name), 70),
                previewProgressEntry(2L, stringResource(R.string.app_name), 30)
            ),
            actionLabel = stringResource(R.string.progress_see_more),
            modifier = Modifier.padding(16.dp)
        )
    }
}
