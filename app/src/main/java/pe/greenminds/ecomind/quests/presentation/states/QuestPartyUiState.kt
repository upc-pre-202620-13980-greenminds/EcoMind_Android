package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation
import pe.greenminds.ecomind.quests.domain.model.CollabQuestState

data class QuestPartyUiState(
    val party: CollabQuestState? = null,
    val invitations: List<CollabQuestInvitation> = emptyList(),
    val names: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val hasActiveAssignment: Boolean = false,
    val canExecute: Boolean = true,
    val canCreateParty: Boolean = true
) {
    val isEnrolled: Boolean get() = party?.currentMember?.status == "ACCEPTED"
    val isOwner: Boolean get() = isEnrolled && party?.currentMember?.role == "OWNER"
}
