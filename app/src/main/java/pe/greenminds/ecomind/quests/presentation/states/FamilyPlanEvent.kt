package pe.greenminds.ecomind.quests.presentation.states

sealed interface FamilyPlanEvent {
    data class DraftOpened(val planId: Long) : FamilyPlanEvent
    data class Activated(val planId: Long) : FamilyPlanEvent
    data class Completed(val planId: Long) : FamilyPlanEvent
    data class Deleted(val planId: Long) : FamilyPlanEvent
}
