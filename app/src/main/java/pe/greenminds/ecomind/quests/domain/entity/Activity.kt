package pe.greenminds.ecomind.quests.domain.entity

data class Activity(
    var id: Long,
    var questId: Long,
    var description: String,
    var activity_order: Int,
    var activityConfig: Map<String, Any>,
    var image: String
)
