package pe.greenminds.ecomind.notifications

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.domain.model.AppNotification
import pe.greenminds.ecomind.notifications.infrastructure.local.LocalNotificationRepository
import pe.greenminds.ecomind.notifications.interfaces.inbox.*
import pe.greenminds.ecomind.settings.domain.model.*
import pe.greenminds.ecomind.settings.infrastructure.local.LocalSettingsRepository
import pe.greenminds.ecomind.settings.interfaces.preferences.*
import pe.greenminds.ecomind.shared.infrastructure.ExperienceDataStore
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

class EngagementTests {
    @get:Rule val compose = createComposeRule()
    @Test fun notificationCanOpenTheMedalCollectionAndMarkAllRead() {
        var opened = ""
        var readAll = false
        compose.setContent { EcoMindTheme { NotificationsContent(NotificationsUiState(isLoading = false, items = listOf(AppNotification("medal", NotificationCategory.ACHIEVEMENTS, 180))), {}, { opened = it.id }, { readAll = true }, {}, {}) } }
        compose.onNodeWithText(label(R.string.notification_medal_title)).performClick()
        compose.runOnIdle { assertEquals("medal", opened) }
        compose.onNodeWithText(label(R.string.notifications_read_all)).performClick()
        compose.runOnIdle { assertTrue(readAll) }
    }
    @Test fun notificationPreferencesOfferAnAccessibleSwitch() {
        var result: Pair<NotificationCategory, Boolean>? = null
        compose.setContent { EcoMindTheme { NotificationPreferencesContent(PreferencesUiState(isLoading = false), {}, { category, enabled -> result = category to enabled }) } }
        compose.onNode(hasText(label(R.string.pref_achievements)) and isToggleable()).assertIsOn().performClick()
        compose.runOnIdle { assertEquals(NotificationCategory.ACHIEVEMENTS to false, result) }
    }
    @Test fun preferencesPersistAcrossRepositoryRecreationAndAreIsolatedByAccount() = runBlocking {
        val store = ExperienceDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
        val sessions = object : SessionRepository {
            val current = MutableStateFlow<Session?>(Session(9001, "test@example.com", "demo", Long.MAX_VALUE))
            override fun getSession() = current
            override suspend fun saveSession(session: Session) { current.value = session }
            override suspend fun clearSession() { current.value = null }
        }
        val repository = LocalSettingsRepository(store, sessions)
        repository.setNotification(NotificationCategory.ACHIEVEMENTS, false).getOrThrow()
        val reopened = LocalSettingsRepository(store, sessions)
        assertFalse(NotificationCategory.ACHIEVEMENTS in reopened.observe().first().enabledNotifications)
        sessions.current.value = sessions.current.value!!.copy(accountId = 9002)
        reopened.setNotification(NotificationCategory.ACHIEVEMENTS, true).getOrThrow()
        assertTrue(NotificationCategory.ACHIEVEMENTS in reopened.observe().first().enabledNotifications)
        sessions.current.value = sessions.current.value!!.copy(accountId = 9001)
        assertFalse(NotificationCategory.ACHIEVEMENTS in reopened.observe().first().enabledNotifications)
        assertTrue(LocalNotificationRepository(store).observe(9001).first().isEmpty())
    }
    private fun label(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
