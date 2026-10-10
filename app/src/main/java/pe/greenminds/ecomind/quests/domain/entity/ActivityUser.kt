package pe.greenminds.ecomind.quests.domain.entity

data class ActivityUser (
    val id:Long,
    val questUserId: Long,
    val activityId: Long,
    val progress: Double,
    val endDate: String?,
    val description: String,
    val configuration: Map<String, Any>,
    val collaborativeSessionId: Long?=null
)