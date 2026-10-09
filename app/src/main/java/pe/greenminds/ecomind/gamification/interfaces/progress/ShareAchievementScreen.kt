package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.interfaces.achievements.*
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun ShareAchievementScreen(awardId: String, onBack: () -> Unit, onSignIn: () -> Unit,
    vm: ShareAchievementViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirm by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(awardId) { vm.load(awardId) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.gamification_share), onBack)
        when {
            state.loading -> LoadingContent()
            state.sessionRequired -> RecoveryContent(true, { vm.load(awardId) }, onSignIn)
            state.simulated -> StateMessage(stringResource(R.string.gamification_share_connected))
            state.failed && state.communities.isEmpty() && state.share == null -> RecoveryContent(false, { vm.load(awardId) }, onSignIn)
            else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.gamification_share_note), style = interTextStyle(14), modifier = Modifier.padding(vertical = 16.dp))
                state.communities.forEach { group ->
                    Row(Modifier.fillMaxWidth().selectable(state.selected == group.id, enabled = !state.submitted && !state.busy,
                        role = Role.RadioButton, onClick = { vm.select(group.id) }).padding(vertical = 8.dp)) {
                        RadioButton(selected = state.selected == group.id, onClick = null)
                        Text(group.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                if (state.communities.isEmpty() && state.share == null) Text(stringResource(R.string.gamification_no_communities))
                state.share?.let { share ->
                    Text(stringResource(when (share.status) {
                        "PUBLISHED" -> R.string.gamification_published
                        "PENDING" -> R.string.gamification_pending
                        else -> R.string.gamification_unsent
                    }), modifier = Modifier.padding(vertical = 16.dp))
                }
                if (state.failed) Text(stringResource(R.string.gamification_share_failed), color = MaterialTheme.colorScheme.error)
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.share?.status != "PUBLISHED") Button(onClick = { confirm = true }, enabled = !state.busy && state.selected != null,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.gamification_share)) }
                TextButton(onClick = { vm.load(awardId) }, enabled = !state.busy) { Text(stringResource(R.string.gamification_refresh)) }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.gamification_share)) },
        text = { Text(stringResource(R.string.gamification_share_confirm)) },
        confirmButton = { TextButton(onClick = { confirm = false; vm.send(awardId) }) { Text(stringResource(R.string.gamification_share)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.gamification_cancel)) } })
}
