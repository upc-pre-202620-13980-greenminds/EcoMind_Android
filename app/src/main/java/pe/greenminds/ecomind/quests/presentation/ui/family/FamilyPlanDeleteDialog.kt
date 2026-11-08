package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.*

@Composable
internal fun FamilyPlanDeleteDialog(
    active: Boolean,
    busy: Boolean,
    hasError: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !busy, dismissOnClickOutside = !busy)) {
        Surface(shape = RoundedCornerShape(22.dp), color = White) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onDismiss, enabled = !busy, modifier = Modifier.align(Alignment.End).size(48.dp)) {
                    Icon(painterResource(R.drawable.ic_close), stringResource(R.string.family_plan_close), Modifier.size(20.dp))
                }
                Text(stringResource(if (active) R.string.family_plan_cancel_question else R.string.family_plan_delete_question),
                    style = interTextStyle(16), textAlign = TextAlign.Center)
                if (hasError) Text(stringResource(R.string.family_plan_error), color = MaterialTheme.colorScheme.error,
                    style = interTextStyle(12), modifier = Modifier.padding(top = 12.dp))
                Spacer(Modifier.height(20.dp))
                FamilyPlanButton(stringResource(R.string.family_plan_no), onDismiss, enabled = !busy)
                FamilyPlanButton(stringResource(R.string.family_plan_yes), onConfirm, enabled = !busy, secondary = true)
            }
        }
    }
}
