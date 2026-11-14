package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.model.PartyPerson
import pe.greenminds.ecomind.quests.presentation.viewmodel.QuestExecutionState
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
internal fun InviteFriendsScreen(
    state: QuestExecutionState,
    onBack: () -> Unit,
    onInvite: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry
) {
    var search by rememberSaveable { mutableStateOf("") }
    val party = state.partyState.party
    val searchLabel = stringResource(R.string.quest_search_placeholder)
    Surface(color = Color.White) {
        QuestRefreshContainer(state.refreshing, onRefresh, enabled = !state.busy) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            PartyBackButton(onBack, Modifier.padding(start = 4.dp))
            Text(
                stringResource(R.string.quest_party_friends),
                style = interTextStyle(24, FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)
            )
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                textStyle = interTextStyle(16).copy(color = TextPrimary),
                cursorBrush = SolidColor(TextPrimary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                    .semantics { contentDescription = searchLabel },
                decorationBox = { field ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 44.dp)
                            .background(Color(0xFFEBEBEB), RoundedCornerShape(28.dp))
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (search.isEmpty()) Text(searchLabel, style = interTextStyle(16), color = Color(0xFF55515C))
                            field()
                        }
                        Spacer(Modifier.width(12.dp))
                        Icon(painterResource(R.drawable.ic_search), contentDescription = null,
                            tint = Color(0xFF55515C), modifier = Modifier.size(22.dp))
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            if (state.error) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(stringResource(R.string.quest_party_action_error), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry, enabled = !state.busy) { Text(stringResource(R.string.quest_try_again)) }
                }
            }
            if (state.friends.isEmpty()) {
                Text(stringResource(R.string.quest_party_no_friends), style = interTextStyle(16), modifier = Modifier.padding(24.dp))
            }
            val query = search.trim()
            val matches = state.friends.filter {
                it.name.orEmpty().contains(query, ignoreCase = true) || it.userId.toString() == query
            }
            if (state.friends.isNotEmpty() && matches.isEmpty()) {
                Text(stringResource(R.string.quest_party_no_matches), style = interTextStyle(16), modifier = Modifier.padding(24.dp))
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 36.dp, end = 36.dp, bottom = 16.dp)
            ) {
                items(matches, key = { it.userId }) { friend ->
                    val name = friend.name ?: stringResource(R.string.quest_party_user, friend.userId)
                    val member = party?.members?.firstOrNull { it.userId == friend.userId }
                    val unavailable = friend.userId in party?.unavailableUserIds.orEmpty()
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                        PartyAvatar(name, size = 36.dp, avatarImageReference = friend.avatarImageReference)
                        Text(
                            name, modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            style = interTextStyle(16), color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        FriendInviteButton(
                            text = stringResource(when {
                                member?.status == "PENDING" -> R.string.quest_party_invited
                                member?.status == "ACCEPTED" -> R.string.quest_party_joined
                                unavailable -> R.string.quest_party_unavailable
                                else -> R.string.quest_party_invite_one
                            }),
                            enabled = !state.busy && !state.partyState.hasError && party?.permissions?.canInvite == true && member == null && !unavailable,
                            onClick = { onInvite(friend.userId) }
                        )
                    }
                }
            }
        }
    }
}

}

@Composable
private fun FriendInviteButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier.widthIn(min = 84.dp, max = 124.dp).heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.shadow(if (enabled) 1.dp else 0.dp, shape)
                .background(if (enabled) EcoGreen else Color(0xFFE0E0E0), shape)
                .padding(horizontal = 12.dp, vertical = 4.dp).defaultMinSize(minWidth = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text, style = interTextStyle(16), color = if (enabled) Color.White else Color(0xFF686868),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "Friends selector · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Selector de amigos · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun InviteFriendsPreview() {
    val friends = listOf("CamiSky", "MateoRun", "AlexNova", "Solina", "ClauTerra", "RafaLuz", "MauricioP", "MiloZone", "KattyPhil", "AleAsto")
        .mapIndexed { index, name -> PartyPerson(10L + index, name) }
    EcoMindTheme {
        InviteFriendsScreen(
            state = QuestExecutionState(partyState = sampleParty("OWNER"), friends = friends),
            onBack = {}, onInvite = {}, onRetry = {}
        )
    }
}
