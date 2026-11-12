package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestMember
import pe.greenminds.ecomind.quests.presentation.states.QuestPartyUiState

@Composable
internal fun PartyMembersScreen(
    state: QuestPartyUiState,
    onBack: () -> Unit,
    onRemove: ((Long) -> Unit)? = null,
    onInvite: (() -> Unit)? = null,
    onCancelParty: (() -> Unit)? = null,
    hasActionError: Boolean = false,
    onRetry: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = onRetry
) {
    var confirmCancel by remember { mutableStateOf(false) }
    Surface {
        QuestRefreshContainer(isRefreshing, onRefresh, enabled = !state.isLoading) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
            PartyBackButton(onBack, Modifier.offset(x = (-12).dp))
            if (hasActionError) {
                Text(stringResource(R.string.quest_party_action_error), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry, enabled = !state.isLoading) { Text(stringResource(R.string.quest_try_again)) }
            }
            Text(
                stringResource(if (state.isOwner) R.string.quest_party_manage else R.string.quest_party_participants),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(24.dp))
                val members = state.party?.members.orEmpty()
                val accepted = members.filter { it.status == "ACCEPTED" }
                PartyMemberList(
                    members = accepted,
                    state = state,
                    heading = "${accepted.size}/${state.party?.counters?.maxInvites ?: 5}",
                    onRemove = onRemove
                )
                val pending = members.filter { it.status == "PENDING" }
                if (state.isOwner && pending.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    PartyMemberList(pending, state, stringResource(R.string.quest_party_pending), onRemove)
                }
            }
            if (state.isOwner) {
                PartyButton(
                    stringResource(R.string.quest_party_invite),
                    !state.isLoading && !state.hasError && state.party?.permissions?.canInvite == true && onInvite != null
                ) { onInvite?.invoke() }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { confirmCancel = true },
                    enabled = !state.isLoading && !state.hasError && state.party?.permissions?.canDeleteSession == true && onCancelParty != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text(stringResource(R.string.quest_party_cancel_party), fontSize = 20.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
    }
    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(stringResource(R.string.quest_party_cancel_party)) },
            text = { Text(stringResource(R.string.quest_party_cancel_warning), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { confirmCancel = false; onCancelParty?.invoke() }) {
                    Text(stringResource(R.string.quest_party_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) { Text(stringResource(R.string.quest_party_cancel)) }
            }
        )
    }
}

@Composable
private fun PartyMemberList(
    members: List<CollabQuestMember>,
    state: QuestPartyUiState,
    heading: String,
    onRemove: ((Long) -> Unit)?
) {
    OutlinedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(heading, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
            members.forEach { member ->
                val isCurrent = member.userId == state.party?.currentMember?.userId
                val person = state.people[member.userId]
                val name = if (isCurrent) stringResource(R.string.quest_party_you)
                    else person?.name ?: stringResource(R.string.quest_party_user, member.userId)
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PartyAvatar(name, pending = member.status == "PENDING", avatarImageReference = person?.avatarImageReference)
                    Spacer(Modifier.width(12.dp))
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    if (member.role == "OWNER") {
                        SuggestionChip(onClick = {}, label = { Text(stringResource(R.string.quest_party_owner), fontSize = 14.sp) })
                    } else if (state.isOwner) {
                        val removeLabel = stringResource(R.string.quest_party_remove, name)
                        TextButton(
                            onClick = { onRemove?.invoke(member.id) },
                            enabled = !state.isLoading && !state.hasError && state.party?.permissions?.canRemoveMembers == true && onRemove != null,
                            modifier = Modifier.semantics { contentDescription = removeLabel }
                        ) { Text("×", fontSize = 22.sp) }
                    }
                }
            }
        }
    }
}
