package pe.greenminds.ecomind.quests.interfaces.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.model.ProgressEntry
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
    onAction: () -> Unit = {}
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
                ProgressRow(entry = entry)
            }

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
private fun ProgressRow(entry: ProgressEntry) {
    // The bar has no number on screen, so the screen reader gets the percentage as text
    val description = stringResource(R.string.progress_entry_description, entry.title, entry.percent)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Text(
            text = entry.title,
            style = interTextStyle(15, FontWeight.Medium),
            color = TextPrimary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProgressSectionCardPreview() {
    EcoMindTheme {
        ProgressSectionCard(
            label = stringResource(R.string.progress_in_progress),
            entries = listOf(
                ProgressEntry(questId = 1L, title = stringResource(R.string.app_name), percent = 70),
                ProgressEntry(questId = 2L, title = stringResource(R.string.app_name), percent = 30)
            ),
            actionLabel = stringResource(R.string.progress_see_more),
            modifier = Modifier.padding(16.dp)
        )
    }
}
