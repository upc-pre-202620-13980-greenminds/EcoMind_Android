package pe.greenminds.ecomind.quests.interfaces.detail
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.gamification.interfaces.achievements.*
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
@Composable
fun QuestDetailScreen(id: Long, onBack: () -> Unit, onSignIn: () -> Unit, onProgress: () -> Unit,
    vm: QuestDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(id) { vm.load(id) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.quest_execution_title), onBack)
        val data = state.execution
        when {
            state.loading -> LoadingContent()
            state.sessionRequired || data == null -> RecoveryContent(state.sessionRequired, { vm.load(id) }, onSignIn)
            else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                if (!BuildConfig.REMOTE_BACKEND) DemoLabel()
                Text(data.quest.title, style = interTextStyle(24), modifier = Modifier.padding(vertical = 16.dp))
                Text(data.quest.description, style = interTextStyle(15), modifier = Modifier.padding(bottom = 16.dp))
                Text(stringResource(R.string.quest_execution_rewards, data.quest.ecopoints, data.quest.gemReward), color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.quest_execution_note), style = interTextStyle(12), modifier = Modifier.padding(vertical = 12.dp))
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.error) {
                    Text(stringResource(R.string.quest_execution_error), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.load(id) }, enabled = !state.busy) { Text(stringResource(R.string.gamification_refresh)) }
                }
                if (data.status == "COMPLETED") {
                    Text(stringResource(R.string.quest_execution_completed), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 16.dp))
                    Button(onClick = onProgress, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.gamification_progress)) }
                } else if (!data.canStart) {
                    Text(stringResource(R.string.quest_execution_unavailable), modifier = Modifier.padding(vertical = 16.dp))
                } else if (data.assignmentId == null) {
                    Button(onClick = { vm.start(id) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.quest_execution_start)) }
                } else {
                    data.steps.forEach { step ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Checkbox(checked = step.done, enabled = !state.busy && step.assignmentId != null,
                                onCheckedChange = { vm.check(id, checkNotNull(step.assignmentId), it) })
                            Text(step.description, modifier = Modifier.padding(top = 12.dp).weight(1f), style = interTextStyle(15))
                        }
                    }
                    Button(onClick = { vm.finish(id) }, enabled = !state.busy && data.status == "READY_TO_COMPLETE", modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.quest_execution_finish))
                    }
                }
            }
        }
    }
}
