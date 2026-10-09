package pe.greenminds.ecomind.quests.infrastructure.mapper

import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.infrastructure.remote.*

fun QuestUserDto.toDomain() = QuestUser(id, userId, questId, QuestStatus.valueOf(status), progress, endDate, collaborativeSessionId)
fun ActivityDto.toDomain() = Activity(id, questId, description, order, ActivityType.valueOf(type), activityConfiguration.orEmpty(), image)
fun ActivityUserDto.toDomain() = ActivityUser(id, questUserId, activityId, progress, endDate, activityDescription.orEmpty(), activityConfiguration.orEmpty(), collaborativeSessionId)
