package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.model.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.presentation.states.QuestPartyUiState
import pe.greenminds.ecomind.quests.presentation.ui.execution.QuestExecutionContent
import pe.greenminds.ecomind.quests.presentation.viewmodel.QuestExecutionState
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

internal fun sampleParty(role: String): QuestPartyUiState {
    val enrolled = role != "NONE"
    val owner = role == "OWNER"
    val members = listOf(
        CollabQuestMember(1L, 10L, 1L, 1L, "OWNER", "ACCEPTED", null, null),
        CollabQuestMember(2L, 10L, 2L, 1L, "PARTICIPANT", "ACCEPTED", null, null),
        CollabQuestMember(3L, 10L, 3L, 1L, "PARTICIPANT", "PENDING", null, null)
    )
    return QuestPartyUiState(
        party = CollabQuestState(
            session = if (enrolled) CollabQuestSession(10L, 18L, 1L, "PENDING", null, null, null) else null,
            members = if (enrolled) members else emptyList(),
            currentMember = if (enrolled) members[if (owner) 0 else 1] else null,
            pendingInvitation = null,
            permissions = CollabQuestPermissions(
                canInvite = !enrolled || owner,
                canStart = !enrolled || owner,
                canAcceptInvitation = !enrolled,
                canLeave = enrolled && !owner,
                canRemoveMembers = owner,
                canDeleteSession = owner
            ),
            counters = CollabQuestCounters(if (enrolled) 2 else 0, if (enrolled) 1 else 0, if (enrolled) 3 else 0, 5),
            unavailableUserIds = emptyList(), source = "COLLABORATIVE", familyPlanId = null, familyPlanItemId = null
        ),
        invitations = listOf(
            CollabQuestInvitation(20L, 30L, 18L, "Time to save up energy!", 4L, if (owner) 1L else 2L),
            CollabQuestInvitation(21L, 31L, 18L, "Time to save up energy!", 5L, if (owner) 1L else 2L)
        ),
        people = listOf(
            PartyPerson(1L, "AleAsto", "avatar_alex"),
            PartyPerson(2L, "CamiSky", "avatar_lunalunette"),
            PartyPerson(3L, "RafaLuz"),
            PartyPerson(4L, "MateoRun", "avatar_sonic"),
            PartyPerson(5L, "Solina")
        ).associateBy { it.userId }
    )
}

@Composable
private fun PartyDetailPreview(role: String) {
    val quest = Quest(
        id = 18L, versionGroupId = 18L, versionNumber = 1,
        publicationStatus = QuestPublicationStatus.PUBLISHED, minigameId = null,
        title = "Time to save up energy!",
        description = "We will turn off the lights and unplug the devices we are not using, learning how small gestures can protect our planet.",
        category = QuestCategory.ENERGY, type = QuestType.COLLABORATIVE,
        reward = QuestReward(0, 50), targetAge = null, estimatedMinutes = 60,
        theme = QuestTheme.COLLABORATIVE, assignedDate = null, imageUrl = null
    )
    EcoMindTheme {
        Surface {
            QuestExecutionContent(
                state = QuestExecutionState(execution = QuestExecution(quest, emptyList(), null, emptyList()), partyState = sampleParty(role)),
                onBack = {}, onRetry = {}, onStart = {}, onCheck = { _, _ -> }, onFinish = {},
                onAcceptInvitation = {}, onDeclineInvitation = {}, onPartyStart = {}, onInviteFriends = {}, onLeaveParty = {}
            )
        }
    }
}

@Preview(name = "No party · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 900)
@Preview(name = "Sin grupo · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun NoPartyPreview() = PartyDetailPreview("NONE")

@Preview(name = "Owner · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 1000)
@Preview(name = "Organizador · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun OwnerPreview() = PartyDetailPreview("OWNER")

@Preview(name = "Participant · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 1000)
@Preview(name = "Participante · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun ParticipantPreview() = PartyDetailPreview("PARTICIPANT")

@Preview(name = "Manage party", locale = "en", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(name = "Administrar grupo", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ManagePartyPreview() {
    EcoMindTheme {
        PartyMembersScreen(sampleParty("OWNER"), onBack = {}, onRemove = {}, onInvite = {}, onCancelParty = {})
    }
}

@Preview(name = "Participants", locale = "en", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(name = "Participantes", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParticipantsPreview() {
    EcoMindTheme { PartyMembersScreen(sampleParty("PARTICIPANT"), onBack = {}) }
}
