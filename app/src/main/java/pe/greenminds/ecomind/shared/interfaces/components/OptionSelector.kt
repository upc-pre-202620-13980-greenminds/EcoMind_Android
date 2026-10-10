package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Group where only one option can be chosen, drawn with the style of the text fields
@Composable
fun OptionSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    val shape = RoundedCornerShape(11.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // The screen reader treats the options as one group of radio buttons
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .semantics {
                    contentDescription = label
                    if (errorMessage != null) error(errorMessage)
                }
        ) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(shape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .then(
                            if (selected) {
                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                            } else {
                                Modifier
                            }
                        )
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onOptionSelected(index) }
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    // The check mark shows the choice without depending only on color
                    if (selected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionSelectorPreview() {
    EcoMindTheme {
        OptionSelector(
            label = stringResource(R.string.sign_up_role_label),
            options = listOf(
                stringResource(R.string.sign_up_role_student),
                stringResource(R.string.sign_up_role_parent)
            ),
            selectedIndex = 0,
            onOptionSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
