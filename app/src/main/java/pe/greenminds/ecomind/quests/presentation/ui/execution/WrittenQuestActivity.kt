package pe.greenminds.ecomind.quests.presentation.ui.execution

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.shared.interfaces.theme.*

@Composable
internal fun WrittenQuestActivity(
    prompt: String,
    assignment: ActivityUser?,
    draft: String?,
    editable: Boolean,
    showError: Boolean,
    onDraftChange: (String) -> Unit
) {
    val savedAnswer = assignment?.responseText
    val answer = if (editable && draft != null) draft else savedAnswer.orEmpty()
    val trimmed = answer.trim { Character.isWhitespace(it) }
    val saved = trimmed == savedAnswer
    val meetsMinimum = assignment?.acceptsWrittenResponse(answer) == true
    val colors = MaterialTheme.colorScheme
    val fieldColor = colors.surfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(prompt)
        OutlinedTextField(
            value = answer,
            onValueChange = onDraftChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = prompt },
            readOnly = !editable || assignment == null,
            isError = showError,
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldColor,
                unfocusedContainerColor = fieldColor,
                errorContainerColor = fieldColor,
                focusedBorderColor = if (meetsMinimum) EcoGreen else colors.outline,
                unfocusedBorderColor = if (meetsMinimum) EcoGreen else colors.outlineVariant,
                cursorColor = EcoGreen
            )
        )
        if (assignment != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    stringResource(R.string.quest_write_minimum,
                        trimmed.codePointCount(0, trimmed.length), assignment.minCharacters),
                    modifier = Modifier.weight(1f),
                    color = if (showError) colors.error else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (saved) {
                    Text(
                        stringResource(R.string.quest_write_saved),
                        color = EcoGreenDark,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
