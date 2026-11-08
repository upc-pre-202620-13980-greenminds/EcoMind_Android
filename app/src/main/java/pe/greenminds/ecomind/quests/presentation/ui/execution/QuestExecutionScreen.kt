package pe.greenminds.ecomind.quests.presentation.ui.execution

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import pe.greenminds.ecomind.quests.presentation.viewmodel.*
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestPartySections
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestPartyActions
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyMembersScreen
import pe.greenminds.ecomind.quests.presentation.ui.invitations.InviteFriendsScreen
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyBackButton
import pe.greenminds.ecomind.quests.presentation.ui.invitations.QuestRefreshContainer

@Composable
fun QuestExecutionScreen(onBack: () -> Unit, viewModel: QuestExecutionViewModel = hiltViewModel(), previewOnly: Boolean = false) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showParticipants by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.partyState.isEnrolled) {
        if (!state.partyState.isEnrolled) showParticipants = false
    }
    LaunchedEffect(state.stage) {
        if (state.stage == QuestStage.STARTING) { delay(2200); viewModel.animationEnded() }
    }
    if (state.selectingFriends) {
        androidx.activity.compose.BackHandler { viewModel.closeFriends() }
        InviteFriendsScreen(state, viewModel::closeFriends, viewModel::inviteFriend, viewModel::openFriends,
            onRefresh = viewModel::refreshScreen)
        return
    }
    if (showParticipants && state.execution?.quest?.type == QuestType.COLLABORATIVE && state.partyState.isEnrolled) {
        androidx.activity.compose.BackHandler { showParticipants = false }
        PartyMembersScreen(
            state.partyState.copy(isLoading = state.busy), onBack = { showParticipants = false },
            onRemove = viewModel::removeMember, onInvite = viewModel::openFriends, onCancelParty = viewModel::cancelParty,
            hasActionError = state.error, onRetry = viewModel::reload,
            isRefreshing = state.refreshing, onRefresh = viewModel::refreshScreen
        )
        return
    }
    QuestExecutionContent(state, onBack, viewModel::reload, viewModel::start, viewModel::check,
        onFinish = if (state.familyExecution != null) onBack else viewModel::finish,
        onOpenParticipants = { showParticipants = true },
        onAcceptInvitation = viewModel::acceptInvitation, onDeclineInvitation = viewModel::declineInvitation,
        onPartyStart = viewModel::startPartyOrSolo, onInviteFriends = viewModel::openFriends, onLeaveParty = viewModel::leaveParty,
        onRefresh = viewModel::refreshScreen, readOnly = previewOnly)
}

@Composable
internal fun QuestExecutionContent(state: QuestExecutionState, onBack: () -> Unit, onRetry: () -> Unit,
    onStart: () -> Unit, onCheck: (Long, Boolean) -> Unit, onFinish: () -> Unit,
    onOpenParticipants: () -> Unit = {},
    onAcceptInvitation: ((Long) -> Unit)? = null,
    onDeclineInvitation: ((Long) -> Unit)? = null,
    onPartyStart: (() -> Unit)? = null,
    onInviteFriends: (() -> Unit)? = null,
    onLeaveParty: (() -> Unit)? = null,
    onRefresh: () -> Unit = onRetry,
    readOnly: Boolean = false) {
    QuestRefreshContainer(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        enabled = !state.busy && state.stage in listOf(QuestStage.DETAIL, QuestStage.ACTIVITIES)
    ) {
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 28.dp)) {
        if (state.stage != QuestStage.STARTING && state.stage != QuestStage.FINISHED) {
            PartyBackButton(onBack, Modifier.offset(x = (-12).dp))
        }
        if (state.error) {
            Text(stringResource(R.string.quest_party_action_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry, enabled = !state.busy) { Text(stringResource(R.string.quest_try_again)) }
        }
        val execution = state.execution
        val family = state.familyExecution
        if (execution == null) {
            return@Column
        }
        if (state.stage == QuestStage.STARTING || state.stage == QuestStage.FINISHED) {
            val finished = state.stage == QuestStage.FINISHED
            Spacer(Modifier.weight(1f))
            Text(if (finished) "Quest completed!\nGood job!" else "Ready... set...\nQuest started!",
                fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            EcoMascot(finished, Modifier.align(Alignment.CenterHorizontally).size(240.dp))
            if (finished) {
                Text("+${execution.quest.reward.ecopoints} ecoPoints", color = EcoGreen, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                if (execution.quest.reward.gems > 0) Text("+${execution.quest.reward.gems} gems", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            Spacer(Modifier.weight(1f))
            if (finished) QuestButton("Nice!", onBack, true, true)
            Spacer(Modifier.height(28.dp))
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(20.dp))
            Text("QUEST", fontSize = 12.sp)
            Text(execution.quest.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                QuestTag(execution.quest.category.name.lowercase(), SunYellow)
                execution.quest.estimatedMinutes?.let { QuestTag("$it min", Color.Gray) }
                QuestTag("${execution.quest.reward.ecopoints} ecoPoints", EcoGreen)
                if (execution.quest.type == pe.greenminds.ecomind.quests.domain.valueobject.QuestType.COLLABORATIVE ||
                    execution.quest.theme == pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme.COLLABORATIVE) {
                    QuestTag(androidx.compose.ui.res.stringResource(pe.greenminds.ecomind.R.string.quest_theme_collaborative), ChipPurple)
                }
            }
            Spacer(Modifier.height(36.dp))
            if (state.stage == QuestStage.DETAIL) {
                Text(execution.quest.description, modifier = Modifier.fillMaxWidth(), fontSize = 16.sp)
                if (!readOnly && !execution.supported && execution.quest.type != QuestType.COLLABORATIVE) Text("This quest doesn't use individual checkboxes yet.", modifier = Modifier.padding(top = 16.dp))
            } else {
                execution.activities.forEach { activity ->
                    val check = execution.checks.firstOrNull { it.activityId == activity.id }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = check != null && check.progress >= 100,
                            onCheckedChange = { checked -> check?.let { onCheck(it.id, checked) } },
                            enabled = check != null && !state.busy && (family == null || family.canCheck) &&
                                execution.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE),
                            colors = CheckboxDefaults.colors(checkedColor = EcoGreen))
                        Text(check?.description?.takeIf { it.isNotBlank() } ?: activity.description, modifier = Modifier.weight(1f))
                    }
                }
                val currentProgress = family?.item?.progress ?: execution.assignment?.progress ?: 0.0
                val progress by animateFloatAsState((currentProgress / 100).toFloat().coerceIn(0f, 1f), label = "Quest progress")
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(16.dp), color = EcoGreen, trackColor = Color(0xFFE5E5E5))
                Text("${currentProgress.toInt()}% completed", fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(32.dp))
            if (!execution.quest.imageUrl.isNullOrBlank()) {
                AsyncImage(model = execution.quest.imageUrl, contentDescription = execution.quest.title, modifier = Modifier.fillMaxWidth().height(200.dp))
            } else {
                EnergyIllustration(Modifier.size(190.dp))
            }
            Spacer(Modifier.height(24.dp))
            if (!readOnly && execution.quest.type == QuestType.COLLABORATIVE && state.stage == QuestStage.DETAIL) {
                QuestPartySections(
                    questId = execution.quest.id,
                    state = state.partyState.copy(isLoading = state.busy || state.partyState.isLoading),
                    onRetry = onRetry,
                    onOpenParticipants = onOpenParticipants,
                    onAccept = onAcceptInvitation,
                    onDecline = onDeclineInvitation
                )
            }
        }
        val completed = execution.assignment?.status == QuestStatus.COMPLETED
        if (readOnly) {
            Spacer(Modifier.height(12.dp))
        } else if (state.stage == QuestStage.DETAIL) {
            if (family != null) {
                QuestButton(if (completed) stringResource(R.string.family_plan_open_activities) else "Resume Quest", onStart,
                    execution.supported && !state.busy && (family.canCheck || completed), false)
            } else if (execution.quest.type == QuestType.COLLABORATIVE) {
                QuestPartyActions(
                    state = state.partyState.copy(isLoading = state.busy || state.partyState.isLoading),
                    onStart = onPartyStart,
                    onInvite = onInviteFriends,
                    onLeave = onLeaveParty
                )
            } else {
                QuestButton(if (completed) "Restart Quest" else if (execution.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)) "Resume Quest" else "Start Quest", onStart, execution.supported && !state.busy, false)
            }
        } else if (family != null) {
            QuestButton("Finish Quest", onFinish, family.readyToReturnToPlan && !state.busy, true)
        } else if (completed) {
            QuestButton("Restart Quest", onStart, execution.supported && !state.busy, false)
        } else {
            QuestButton("Finish Quest", onFinish, execution.canFinish && !state.busy, true)
        }
        Spacer(Modifier.height(28.dp))
    }
    }
}

@Composable
private fun QuestTag(text: String, color: Color) {
    Text(text, color = Color.White, fontSize = 10.sp, modifier = Modifier.background(color, RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 5.dp))
}
@Composable
private fun QuestButton(text: String, action: () -> Unit, enabled: Boolean, green: Boolean) {
    PillButton(text, action, if (!enabled) Color(0xFFA4A4A4) else if (green) LeafGreen else SkyBlue,
        if (!enabled) Color.Gray else if (green) LeafGreenDark else SkyBlueDark, height = 58.dp, enabled = enabled)
}
@Composable
private fun EnergyIllustration(modifier: Modifier) {
    Canvas(modifier) {
        val s = size.width
        drawOval(Color.LightGray.copy(alpha = .35f), Offset(s * .08f, s * .85f), androidx.compose.ui.geometry.Size(s * .84f, s * .08f))
        drawRoundRect(EcoGreen, Offset(s * .58f, s * .30f), androidx.compose.ui.geometry.Size(s * .30f, s * .55f), androidx.compose.ui.geometry.CornerRadius(s * .06f))
        drawRect(Color.DarkGray, Offset(s * .68f, s * .25f), androidx.compose.ui.geometry.Size(s * .10f, s * .05f))
        drawCircle(Color(0xFFFFEB8A), s * .21f, Offset(s * .32f, s * .41f))
        drawCircle(SunYellow, s * .21f, Offset(s * .32f, s * .41f), style = Stroke(4.dp.toPx()))
        drawRect(Color.Gray, Offset(s * .23f, s * .64f), androidx.compose.ui.geometry.Size(s * .18f, s * .20f))
        drawLine(Color.White, Offset(s * .77f, s * .41f), Offset(s * .66f, s * .57f), 6.dp.toPx())
        drawLine(Color.White, Offset(s * .66f, s * .57f), Offset(s * .77f, s * .57f), 6.dp.toPx())
        drawLine(Color.White, Offset(s * .77f, s * .57f), Offset(s * .66f, s * .74f), 6.dp.toPx())
    }
}
@Composable
internal fun EcoMascot(confetti: Boolean, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "Quest mascot")
    val hop by transition.animateFloat(6f, -8f,
        infiniteRepeatable(tween(760), RepeatMode.Reverse), label = "Started hop")
    val hopScale by transition.animateFloat(.98f, 1.02f,
        infiniteRepeatable(tween(760), RepeatMode.Reverse), label = "Started scale")
    val celebration = remember { Animatable(0f) }
    LaunchedEffect(confetti) {
        if (confetti) celebration.animateTo(1f, tween(520))
    }
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(
            if (confetti) pe.greenminds.ecomind.R.drawable.img_quest_completed
            else pe.greenminds.ecomind.R.drawable.img_quest_started),
        contentDescription = null,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        modifier = modifier.then(Modifier.graphicsLayer {
            if (confetti) {
                alpha = celebration.value
                scaleX = .82f + .18f * celebration.value
                scaleY = scaleX
                rotationZ = -3f * (1f - celebration.value)
            } else {
                translationY = hop.dp.toPx()
                scaleX = hopScale
                scaleY = hopScale
            }
        })
    )
}
