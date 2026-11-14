package pe.greenminds.ecomind.quests.infrastructure.offline

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.application.service.QuestExecutionService
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.ActivityType
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import pe.greenminds.ecomind.shared.infrastructure.offline.Connectivity
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineDao
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineQuestRecord
import retrofit2.HttpException

enum class OfflineQuestStatus { SAVED, EDITED, PENDING, CONFIRMED, REJECTED }

data class OfflineQuest(
    val execution: QuestExecution,
    val changes: Map<Long, Boolean> = emptyMap(),
    val status: OfflineQuestStatus = OfflineQuestStatus.SAVED,
    val reward: ConfirmedQuestReward? = null
) {
    val isAvailableOffline: Boolean
        get() = execution.assignment?.status != QuestStatus.COMPLETED || status != OfflineQuestStatus.SAVED
}

data class ConfirmedQuestReward(val ecopoints: Long, val gems: Int)

data class QuestSyncResult(val retry: Boolean, val rewardConfirmed: Boolean)

private val activeStatuses = setOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)

@Singleton
class OfflineQuestStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: OfflineDao,
    private val sessions: SessionRepository,
    private val api: QuestExecutionApi
) {
    private val connectivity = Connectivity(context)
    private val mutex = Mutex()
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { date, _, _ -> JsonPrimitive(date.toString()) })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer<LocalDate> { date, _, _ -> LocalDate.parse(date.asString) })
        .create()

    suspend fun session(): Session? = sessions.getSession().first()

    suspend fun get(questId: Long): OfflineQuest? =
        session()?.let { dao.quest(it.accountId, questId)?.decode() }

    fun observe(accountId: Long) = dao.observeQuests(accountId).map { records -> records.map { it.decode() } }

    suspend fun load(questId: Long, service: QuestExecutionService): QuestExecution {
        val session = session()
        val cached = session?.let { dao.quest(it.accountId, questId)?.decode() }
        if (cached != null && (!connectivity.connected || session.isExpired(System.currentTimeMillis()) ||
                cached.status in setOf(OfflineQuestStatus.EDITED, OfflineQuestStatus.PENDING, OfflineQuestStatus.REJECTED))
        ) {
            return cached.execution
        }
        if (!connectivity.connected) throw IOException("This quest needs a connection")
        return try {
            service.load(questId).also { fresh ->
                remember(fresh)
                if (cached?.status == OfflineQuestStatus.SAVED && !fresh.canSaveOffline(session.accountId)) {
                    mutex.withLock {
                        val record = dao.quest(session.accountId, questId)?.decode()
                        if (record == cached) dao.deleteQuest(session.accountId, questId)
                    }
                }
            }
        } catch (error: IOException) {
            cached?.execution ?: throw error
        } catch (error: HttpException) {
            if (error.code() < 500) throw error
            cached?.execution ?: throw error
        }
    }

    suspend fun remember(execution: QuestExecution) {
        val accountId = session()?.accountId ?: return
        if (!execution.canSaveOffline(accountId)) return
        mutex.withLock {
            val existing = dao.quest(accountId, execution.quest.id)?.decode()
            val newAssignment = existing?.execution?.assignment?.id != execution.assignment?.id
            if (existing == null || existing.status == OfflineQuestStatus.SAVED ||
                newAssignment && existing.status in setOf(OfflineQuestStatus.CONFIRMED, OfflineQuestStatus.REJECTED)
            ) {
                save(accountId, OfflineQuest(execution))
            }
        }
    }

    suspend fun prepare(catalog: List<Quest>) {
        val session = session()?.takeUnless { it.isExpired(System.currentTimeMillis()) } ?: return
        val candidates = catalog.filter { it.type == QuestType.ACTIVITIES }.associateBy { it.id }
        val assignments = activeStatuses.flatMap { api.getByStatus(it.name, session) }
            .filter { it.userId == session.accountId && it.collaborativeSessionId == null && it.questId in candidates }
            .distinctBy { it.questId }
            .take(12)
        for (assignment in assignments) {
            val existing = dao.quest(session.accountId, assignment.questId)?.decode()
            if (existing?.execution?.assignment?.id == assignment.id) continue
            val quest = candidates.getValue(assignment.questId)
            val activities = api.activities(quest.id).map { it.toDomain() }.sortedBy { it.order }
            if (activities.isEmpty() || activities.any { it.type != ActivityType.CHECKBOX }) continue
            val checks = api.activityUsers(assignment.id, session).map { it.toDomain() }
            remember(QuestExecution(quest, activities, assignment.toDomain(), checks))
        }
    }

    suspend fun check(execution: QuestExecution, activityUserId: Long, checked: Boolean): QuestExecution? {
        val accountId = session()?.accountId ?: return null
        if (!execution.canSaveOffline(accountId)) return null
        val updated = mutex.withLock {
            val current = currentRecord(accountId, execution)
            check(current.status in setOf(OfflineQuestStatus.SAVED, OfflineQuestStatus.EDITED))
            val checks = current.execution.checks.map {
                if (it.id == activityUserId) it.copy(progress = if (checked) 100.0 else 0.0) else it
            }
            require(checks.any { it.id == activityUserId })
            val completed = current.execution.activities.count { activity ->
                checks.any { it.activityId == activity.id && it.progress >= 100 }
            }
            val progress = completed * 100.0 / current.execution.activities.size
            val assignment = requireNotNull(current.execution.assignment)
            current.copy(
                execution = current.execution.copy(
                    assignment = assignment.copy(
                        status = if (progress == 100.0) QuestStatus.READY_TO_COMPLETE else QuestStatus.IN_PROGRESS,
                        progress = progress
                    ),
                    checks = checks
                ),
                changes = current.changes + (activityUserId to checked),
                status = OfflineQuestStatus.EDITED
            ).also { save(accountId, it) }
        }
        schedule(accountId)
        return updated.execution
    }

    suspend fun finish(execution: QuestExecution): Boolean {
        val accountId = session()?.accountId ?: return false
        if (!execution.canSaveOffline(accountId)) return false
        mutex.withLock {
            val current = currentRecord(accountId, execution)
            check(current.execution.canFinish)
            check(current.status in setOf(OfflineQuestStatus.SAVED, OfflineQuestStatus.EDITED))
            save(accountId, current.copy(status = OfflineQuestStatus.PENDING))
        }
        schedule(accountId)
        return true
    }

    private suspend fun currentRecord(accountId: Long, execution: QuestExecution): OfflineQuest {
        val cached = dao.quest(accountId, execution.quest.id)?.decode()
        return cached?.takeIf { it.execution.assignment?.id == execution.assignment?.id } ?: OfflineQuest(execution)
    }

    fun schedule(accountId: Long) {
        val request = OneTimeWorkRequestBuilder<QuestSyncWorker>()
            .setInputData(workDataOf(QuestSyncWorker.ACCOUNT_ID to accountId))
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "offline-quests-$accountId", ExistingWorkPolicy.APPEND_OR_REPLACE, request
        )
    }

    suspend fun resume(session: Session) {
        if (!session.isExpired(System.currentTimeMillis()) && dao.pendingQuests(session.accountId).isNotEmpty()) {
            schedule(session.accountId)
        }
    }

    suspend fun sync(session: Session, send: suspend (OfflineQuest) -> OfflineQuest): QuestSyncResult {
        var retry = false
        var confirmed = false
        for (record in dao.pendingQuests(session.accountId)) {
            if (session() != session || session.isExpired(System.currentTimeMillis())) break
            val current = record.decode()
            val result = try {
                send(current)
            } catch (_: IOException) {
                retry = true
                continue
            } catch (error: HttpException) {
                when (error.code()) {
                    401 -> break
                    400, 403, 404, 409, 410, 422 -> current.copy(status = OfflineQuestStatus.REJECTED)
                    else -> {
                        retry = true
                        continue
                    }
                }
            }
            mutex.withLock {
                if (session() != session) return QuestSyncResult(retry = false, rewardConfirmed = false)
                // A new checkbox edit or completion may have been saved while the request was in flight.
                if (dao.quest(session.accountId, record.questId) == record) {
                    save(session.accountId, result)
                    retry = retry || result.status == OfflineQuestStatus.PENDING
                    confirmed = confirmed || result.status == OfflineQuestStatus.CONFIRMED
                }
            }
        }
        return QuestSyncResult(retry, confirmed)
    }

    private fun OfflineQuestRecord.decode() = gson.fromJson(data, OfflineQuest::class.java)

    private suspend fun save(accountId: Long, quest: OfflineQuest) {
        dao.saveQuest(OfflineQuestRecord(accountId, quest.execution.quest.id, gson.toJson(quest), quest.status.name))
    }
}

private fun QuestExecution.canSaveOffline(accountId: Long): Boolean =
    quest.type == QuestType.ACTIVITIES && familyPlanId == null &&
        assignment?.userId == accountId && assignment.collaborativeSessionId == null &&
        assignment.status in activeStatuses && activities.isNotEmpty() &&
        activities.all { activity ->
            activity.type == ActivityType.CHECKBOX && checks.any { it.activityId == activity.id }
        }
