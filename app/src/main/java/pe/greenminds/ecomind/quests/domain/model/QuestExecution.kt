package pe.greenminds.ecomind.quests.domain.model

data class ActivityStep(val id: Long, val assignmentId: Long?, val description: String, val type: String, val done: Boolean)
data class QuestExecution(val quest: Quest, val assignmentId: Long?, val status: String?, val steps: List<ActivityStep>) {
    val canStart: Boolean get() = quest.type in setOf(QuestType.ACTIVITIES, QuestType.DAILY_QUEST) && steps.isNotEmpty() && steps.all { it.type == "CHECKBOX" }
}
