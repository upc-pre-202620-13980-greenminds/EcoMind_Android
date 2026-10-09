package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Row of tabs inside a rounded container; the selected one is a filled pill
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(24.dp)
    val tabShape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, containerShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, containerShape)
            .padding(4.dp)
            .selectableGroup()
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    // The design draws 32dp; 40dp inside a 48dp container keeps the touch target
                    .heightIn(min = 40.dp)
                    .clip(tabShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onOptionSelected(index) }
                    )
            ) {
                Text(
                    text = option,
                    maxLines = 1,
                    style = interTextStyle(
                        sizeSp = 12,
                        weight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SegmentedTabsPreview() {
    EcoMindTheme {
        SegmentedTabs(
            options = listOf(
                stringResource(R.string.nav_profile),
                stringResource(R.string.nav_community),
                stringResource(R.string.nav_ranking)
            ),
            selectedIndex = 0,
            onOptionSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
