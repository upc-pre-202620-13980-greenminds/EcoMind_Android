package pe.greenminds.ecomind.quests

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.application.ProgressOverview
import pe.greenminds.ecomind.quests.application.usecase.GetProgressOverviewUseCase
import pe.greenminds.ecomind.quests.infrastructure.implementation.LocalQuestProgressRepository
import pe.greenminds.ecomind.quests.domain.entity.Activity
import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStatus
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStore
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineDatabase
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineSnapshot
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineSnapshots
import com.google.gson.Gson
import retrofit2.Retrofit

class OfflineQuestStorageTest {
    @Test
    fun pendingChecklistSurvivesReopeningDatabaseAndIsScopedToItsAccount(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "offline-test-${UUID.randomUUID()}.db"
        val originalSession = Session(Long.MAX_VALUE, "test@example.com", "fake", 0)
        val sessions = TestSessions(originalSession)
        val overview = GetProgressOverviewUseCase(LocalQuestProgressRepository())().getOrThrow()
        val api = Retrofit.Builder().baseUrl("http://localhost/").build().create(QuestExecutionApi::class.java)
        val quest = Quest(
            17, 17, 1, QuestPublicationStatus.PUBLISHED, null, "Checklist", "Description",
            QuestCategory.ENERGY, QuestType.ACTIVITIES, QuestReward(2, 20), null, null,
            QuestTheme.CHECKBOX, LocalDate.of(2026, 11, 13), null
        )
        val execution = QuestExecution(
            quest,
            listOf(Activity(8, 17, "Switch off the lights", 1, ActivityType.CHECKBOX, emptyMap(), null)),
            QuestUser(15, originalSession.accountId, 17, QuestStatus.IN_PROGRESS, 0.0, null),
            listOf(ActivityUser(21, 15, 8, 0.0, null, "Switch off the lights", emptyMap()))
        )
        try {
            val database = Room.databaseBuilder(context, OfflineDatabase::class.java, name).build()
            try {
                val store = OfflineQuestStore(context, database.offlineDao(), sessions, api)
                store.remember(execution)
                val checked = requireNotNull(store.check(execution, 21, true))
                store.finish(checked)
                database.offlineDao().saveSnapshot(
                    OfflineSnapshot(originalSession.accountId, "quest-progress", Gson().toJson(overview))
                )
            } finally {
                database.close()
            }
            val reopened = Room.databaseBuilder(context, OfflineDatabase::class.java, name).build()
            try {
                val store = OfflineQuestStore(context, reopened.offlineDao(), sessions, api)
                val saved = requireNotNull(store.get(17))
                assertEquals(OfflineQuestStatus.PENDING, saved.status)
                assertEquals(mapOf(21L to true), saved.changes)
                assertEquals(100.0, saved.execution.assignment?.progress)
                assertEquals(quest.assignedDate, saved.execution.quest.assignedDate)
                assertNull(saved.reward)
                val snapshots = OfflineSnapshots(context, reopened.offlineDao(), sessions)
                val progress = snapshots.fetch<ProgressOverview>("quest-progress", ProgressOverview::class.java) {
                    error("Expired sessions must read progress from disk")
                }.getOrThrow()
                assertEquals(overview, progress)
                sessions.saveSession(originalSession.copy(accountId = Long.MAX_VALUE - 1))
                assertNull(store.get(17))
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private class TestSessions(session: Session) : SessionRepository {
        private val current = MutableStateFlow<Session?>(session)
        override fun getSession() = current
        override suspend fun saveSession(session: Session) { current.value = session }
        override suspend fun clearSession() { current.value = null }
    }
}
