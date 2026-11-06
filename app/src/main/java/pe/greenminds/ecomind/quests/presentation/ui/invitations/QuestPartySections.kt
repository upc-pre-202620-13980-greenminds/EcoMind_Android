package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestMember
import pe.greenminds.ecomind.quests.presentation.states.QuestPartyUiState
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark

@Composable
internal fun QuestPartySections(
    questId: Long,
    state: QuestPartyUiState,
    onRetry: () -> Unit,
    onOpenParticipants: () -> Unit,
    onAccept: ((Long) -> Unit)? = null,
    onDecline: ((Long) -> Unit)? = null
) {
    if (state.hasError) {
        Text(stringResource(R.string.quest_party_load_error))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.quest_try_again)) }
    }
    val invitations = state.invitations.filter { it.questId == questId }
    if (invitations.isNotEmpty()) {
        var expanded by rememberSaveable(questId) { mutableStateOf(true) }
        OutlinedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Text(stringResource(R.string.quest_party_pending), Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
                    Text(if (expanded) "⌃" else "⌄")
                }
                if (expanded) {
                    invitations.forEach { invitation ->
                        val name = state.names[invitation.organizerUserId]
                            ?: stringResource(R.string.quest_party_user, invitation.organizerUserId)
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            PartyAvatar(name)
                            Spacer(Modifier.width(10.dp))
                            Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Button(
                                onClick = { onAccept?.invoke(invitation.memberId) },
                                enabled = !state.isLoading && !state.hasError && !state.isEnrolled && !state.hasActiveAssignment && onAccept != null,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF7DB75E)),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text(stringResource(R.string.quest_party_accept), fontSize = 16.sp) }
                            val declineLabel = stringResource(R.string.quest_party_decline)
                            TextButton(
                                onClick = { onDecline?.invoke(invitation.memberId) },
                                enabled = !state.isLoading && !state.hasError && onDecline != null,
                                modifier = Modifier.semantics { contentDescription = declineLabel },
                                contentPadding = PaddingValues(8.dp)
                            ) { Text("×", fontSize = 22.sp) }
                        }
                    }
                    if (state.isEnrolled || state.hasActiveAssignment) {
                        Text(stringResource(R.string.quest_party_accept_blocked), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 21.sp))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    if (state.isEnrolled) {
        OutlinedCard(
            onClick = onOpenParticipants,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.quest_party_participants), Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
                    Text("›", fontSize = 24.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.party?.members?.filter { it.status == "ACCEPTED" || it.status == "PENDING" }
                        ?.sortedBy { if (it.status == "ACCEPTED") 0 else 1 }?.forEach { member ->
                        val name = state.names[member.userId] ?: stringResource(R.string.quest_party_user, member.userId)
                        PartyAvatar(name, pending = member.status == "PENDING")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun QuestPartyActions(
    state: QuestPartyUiState,
    onStart: (() -> Unit)? = null,
    onInvite: (() -> Unit)? = null,
    onLeave: (() -> Unit)? = null
) {
    var dialog by remember { mutableStateOf<String?>(null) }
    val permissions = state.party?.permissions
    val ready = !state.isLoading && !state.hasError
    val canStart = ready && state.canExecute && (state.hasActiveAssignment ||
        if (state.isEnrolled) state.isOwner && permissions?.canStart == true else state.canCreateParty)
    val canInvite = ready && (if (state.isEnrolled) state.isOwner && permissions?.canInvite == true else state.canCreateParty)
    PartyButton(stringResource(if (state.hasActiveAssignment) R.string.quest_party_resume else R.string.quest_party_start), canStart && onStart != null) {
        when {
            state.hasActiveAssignment -> onStart?.invoke()
            state.isOwner && (state.party?.counters?.pendingInvites ?: 0) > 0 -> dialog = "startOwner"
            !state.isEnrolled && state.invitations.isNotEmpty() -> dialog = "startSolo"
            else -> onStart?.invoke()
        }
    }
    Spacer(Modifier.height(10.dp))
    if (state.isEnrolled && (!state.isOwner || state.party?.session?.status == "STARTED")) {
        PartyButton(stringResource(R.string.quest_party_leave), ready && permissions?.canLeave == true && onLeave != null) {
            dialog = "leave"
        }
    } else {
        PartyButton(stringResource(R.string.quest_party_invite), canInvite && onInvite != null) {
            if (!state.isEnrolled && state.invitations.isNotEmpty()) dialog = "invite" else onInvite?.invoke()
        }
    }
    if (!state.canExecute && !state.hasError) {
        Text(stringResource(R.string.quest_party_unsupported), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 21.sp))
    }
    val activeDialog = dialog
    if (activeDialog != null) {
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.quest_party_confirm_title)) },
            text = {
                Text(stringResource(when (activeDialog) {
                    "leave" -> R.string.quest_party_leave_warning
                    "startOwner" -> R.string.quest_party_start_warning
                    else -> R.string.quest_party_received_warning
                }), style = MaterialTheme.typography.bodyLarge)
            },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    when (activeDialog) {
                        "leave" -> onLeave?.invoke()
                        "invite" -> onInvite?.invoke()
                        else -> onStart?.invoke()
                    }
                }) { Text(stringResource(R.string.quest_party_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.quest_party_cancel)) }
            }
        )
    }
}

@Composable
internal fun PartyButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    PillButton(
        text = text,
        onClick = onClick,
        faceColor = if (enabled) SkyBlue else androidx.compose.ui.graphics.Color(0xFFA4A4A4),
        baseColor = if (enabled) SkyBlueDark else androidx.compose.ui.graphics.Color.Gray,
        height = 63.dp,
        textStyle = MaterialTheme.typography.headlineSmall,
        enabled = enabled
    )
}

@Composable
internal fun PartyAvatar(name: String, pending: Boolean = false, size: Dp = 40.dp) {
    val label = if (pending) stringResource(R.string.quest_party_awaiting_reply, name) else name
    Box(
        Modifier.size(size).alpha(if (pending) 0.45f else 1f)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold)
    }
}
