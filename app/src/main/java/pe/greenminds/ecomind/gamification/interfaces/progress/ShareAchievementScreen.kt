package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.interfaces.achievements.AchievementHeader
import pe.greenminds.ecomind.gamification.interfaces.achievements.LoadingContent
import pe.greenminds.ecomind.gamification.interfaces.achievements.RecoveryContent
import pe.greenminds.ecomind.gamification.interfaces.achievements.StateMessage
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun ShareAchievementScreen(awardId: String, onBack: () -> Unit, onSignIn: () -> Unit,
    vm: ShareAchievementViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirm by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(awardId) { vm.loadShare(awardId) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.gamification_share), onBack)
        when {
            state.isLoading -> LoadingContent()
            state.sessionRequired -> RecoveryContent(true, { vm.loadShare(awardId) }, onSignIn)
            state.isSimulated -> StateMessage(stringResource(R.string.gamification_share_connected))
            state.hasError && state.communities.isEmpty() && state.share == null -> RecoveryContent(false, { vm.loadShare(awardId) }, onSignIn)
            else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.gamification_share_note), style = interTextStyle(14), modifier = Modifier.padding(vertical = 16.dp))
                state.communities.forEach { group ->
                    Row(Modifier.fillMaxWidth().selectable(state.selectedCommunityId == group.id, enabled = !state.isSubmitted && !state.isSharing,
                        role = Role.RadioButton, onClick = { vm.onCommunitySelected(group.id) }).padding(vertical = 8.dp)) {
                        RadioButton(selected = state.selectedCommunityId == group.id, onClick = null)
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
                if (state.hasError) Text(stringResource(R.string.gamification_share_failed), color = MaterialTheme.colorScheme.error)
                if (state.isSharing) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.share?.status != "PUBLISHED") Button(onClick = { confirm = true }, enabled = !state.isSharing && state.selectedCommunityId != null,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.gamification_share)) }
                TextButton(onClick = { vm.loadShare(awardId) }, enabled = !state.isSharing) { Text(stringResource(R.string.gamification_refresh)) }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.gamification_share)) },
        text = { Text(stringResource(R.string.gamification_share_confirm)) },
        confirmButton = { TextButton(onClick = { confirm = false; vm.share(awardId) }) { Text(stringResource(R.string.gamification_share)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.gamification_cancel)) } })
}
