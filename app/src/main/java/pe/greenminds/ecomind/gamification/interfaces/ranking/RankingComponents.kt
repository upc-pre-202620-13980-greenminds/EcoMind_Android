package pe.greenminds.ecomind.gamification.interfaces.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.RankingEntry
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.SurfaceTint
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Positions drawn in green, as in the design
private const val PODIUM_SIZE = 3

// Chips to choose one period; they scroll sideways when they do not fit
@Composable
fun PeriodChips(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(14.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup()
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val color = if (selected) MaterialTheme.colorScheme.primary else TextSecondary

            // The outer box keeps a touch target of 48dp around the smaller chip
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onOptionSelected(index) }
                    )
            ) {
                Text(
                    text = option,
                    style = interTextStyle(12, if (selected) FontWeight.Bold else FontWeight.Normal),
                    color = color,
                    modifier = Modifier
                        .clip(chipShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                White
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else CardBorder,
                            shape = chipShape
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// Card with the place of the user or of their family
@Composable
fun CurrentPositionCard(
    label: String,
    entry: RankingEntry,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(SurfaceTint, shape)
            .border(1.dp, CardBorder, shape)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = interTextStyle(14),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.ranking_position_value, entry.position, entry.ecopoints),
            style = interTextStyle(14, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RankingRow(
    entry: RankingEntry,
    displayName: String,
    modifier: Modifier = Modifier
) {
    val rowShape = RoundedCornerShape(12.dp)
    val isHighlighted = entry.position <= PODIUM_SIZE || entry.isCurrentUser
    // The screen reader says the whole row as one sentence
    val description = stringResource(
        R.string.ranking_row_description,
        entry.position,
        displayName,
        entry.ecopoints
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .then(
                    if (entry.isCurrentUser) {
                        Modifier.background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            rowShape
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp)
                .clearAndSetSemantics { contentDescription = description }
        ) {
            Text(
                text = entry.position.toString(),
                style = interTextStyle(14, FontWeight.Bold),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )
            Text(
                text = displayName,
                style = interTextStyle(14, FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Text(
                text = entry.ecopoints.toString(),
                style = interTextStyle(14, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        // The row of the user has its own background instead of a line
        if (!entry.isCurrentUser) {
            HorizontalDivider(thickness = 1.dp, color = RowDivider)
        }
    }
}
