package pe.greenminds.ecomind.quests.infrastructure.offline

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import pe.greenminds.ecomind.gamification.infrastructure.remote.GamificationApi
import pe.greenminds.ecomind.gamification.infrastructure.remote.collectPages
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.ActivitySubmissionRequest
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.shared.application.GemBalanceStore

class QuestSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun offlineQuests(): OfflineQuestStore
        fun questApi(): QuestExecutionApi
        fun gamificationApi(): GamificationApi
        fun currentProfile(): GetCurrentProfileUseCase
        fun gemBalance(): GemBalanceStore
    }

    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val store = dependencies.offlineQuests()
        val accountId = inputData.getLong(ACCOUNT_ID, -1L)
        val session = store.session()?.takeIf {
            it.accountId == accountId && !it.isExpired(System.currentTimeMillis())
        } ?: return Result.success()
        val result = store.sync(session) { queued ->
            syncQuest(queued, session, dependencies.questApi(), dependencies.gamificationApi())
        }
        if (result.rewardConfirmed && store.session() == session) {
            val profile = dependencies.currentProfile()().getOrNull()
            if (profile != null && profile.id == accountId && store.session() == session) {
                dependencies.gemBalance().update(profile.gemBalance)
            }
        }
        return if (result.retry) Result.retry() else Result.success()
    }

    companion object {
        const val ACCOUNT_ID = "account_id"
    }
}

internal suspend fun syncQuest(
    queued: OfflineQuest,
    session: Session,
    quests: QuestExecutionApi,
    gamification: GamificationApi
): OfflineQuest {
    val assignmentId = requireNotNull(queued.execution.assignment).id
    var assignment = quests.get(assignmentId, session).toDomain()
    if (assignment.userId != session.accountId || assignment.questId != queued.execution.quest.id ||
        assignment.collaborativeSessionId != null
    ) return queued.copy(status = OfflineQuestStatus.REJECTED)
    var checks = if (queued.changes.isEmpty()) queued.execution.checks
        else quests.activityUsers(assignmentId, session).map { it.toDomain() }
    if (assignment.status in setOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)) {
        for ((id, checked) in queued.changes) {
            val remote = checks.firstOrNull { it.id == id }
                ?: return queued.copy(status = OfflineQuestStatus.REJECTED)
            if ((remote.progress >= 100) != checked) {
                quests.submit(id, ActivitySubmissionRequest(mapOf("checked" to checked)), session)
            }
        }
        if (queued.changes.isNotEmpty()) {
            assignment = quests.get(assignmentId, session).toDomain()
            checks = quests.activityUsers(assignmentId, session).map { it.toDomain() }
        }
        if (queued.status == OfflineQuestStatus.PENDING && assignment.status == QuestStatus.READY_TO_COMPLETE) {
            assignment = quests.finish(assignmentId, session).toDomain()
        }
    }

    val updated = queued.copy(execution = queued.execution.copy(assignment = assignment, checks = checks))
    if (queued.status == OfflineQuestStatus.EDITED) {
        return when (assignment.status) {
            QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE, QuestStatus.COMPLETED ->
                updated.copy(changes = emptyMap(), status = OfflineQuestStatus.SAVED)
            else -> queued.copy(status = OfflineQuestStatus.REJECTED)
        }
    }
    if (assignment.status != QuestStatus.COMPLETED) return queued.copy(status = OfflineQuestStatus.REJECTED)

    val date = LocalDate.parse(requireNotNull(assignment.endDate))
    val zone = ZoneId.of("America/Lima")
    val from = date.atStartOfDay(zone).toInstant().toString()
    val to = date.plusDays(1).atStartOfDay(zone).toInstant().toString()
    val executionId = UUID.nameUUIDFromBytes("quest-user:$assignmentId".toByteArray(Charsets.UTF_8)).toString()
    val reward = collectPages { gamification.history(session, from, to, it) }
        .firstOrNull { it.source.type == "QUEST" && it.source.executionId == executionId }
        ?.grantedReward ?: return updated.copy(changes = emptyMap())
    return updated.copy(
        changes = emptyMap(),
        status = OfflineQuestStatus.CONFIRMED,
        reward = ConfirmedQuestReward(reward.ecopoints, reward.gems)
    )
}
