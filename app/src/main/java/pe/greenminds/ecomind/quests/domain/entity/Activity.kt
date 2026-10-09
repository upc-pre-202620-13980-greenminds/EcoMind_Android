package pe.greenminds.ecomind.quests.domain.entity

import pe.greenminds.ecomind.quests.domain.valueobject.ActivityType

data class Activity(val id: Long, val questId: Long, val description: String,
    val order: Int, val type: ActivityType, val configuration: Map<String, Any>, val image: String?)
