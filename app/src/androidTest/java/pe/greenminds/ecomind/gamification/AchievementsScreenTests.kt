package pe.greenminds.ecomind.gamification

import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import pe.greenminds.ecomind.R
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.MutableStateFlow
import pe.greenminds.ecomind.gamification.application.GetAchievementByIdUseCase
import pe.greenminds.ecomind.gamification.application.GetAchievementsUseCase
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.gamification.interfaces.achievements.*
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

class AchievementsScreenTests {
    @get:Rule val compose = createComposeRule()

    @Test
    fun hu011StudentCanOpenAnEarnedMedalAndBrowsePendingOnes() {
        var opened: String? = null
        compose.setContent {
            EcoMindTheme {
                AchievementsContent(loaded(), {}, { opened = it }, {}, {})
            }
        }
        compose.onNodeWithText("First medal").performClick()
        compose.runOnIdle { assertEquals("earned", opened) }
        compose.onNodeWithText(label(R.string.achievements_available)).performClick()
        compose.onNodeWithText("Next medal").assertIsDisplayed()
        compose.onNodeWithText("First medal").assertDoesNotExist()
        compose.onNodeWithText(label(R.string.achievements_demo)).assertIsDisplayed()
    }

    @Test
    fun failedReadOffersRetryAndNeverShowsAnEmptySuccessMessage() {
        var retried = false
        compose.setContent {
            EcoMindTheme {
                AchievementsContent(AchievementsUiState(isLoading = false, hasError = true), {}, {}, { retried = true }, {})
            }
        }
        compose.onNodeWithText(label(R.string.ranking_try_again)).performClick()
        compose.runOnIdle { assertEquals(true, retried) }
        compose.onNodeWithText(label(R.string.achievements_error)).assertIsDisplayed()
    }

    @Test
    fun unknownMedalProvidesARecoverableMissingState() {
        compose.setContent {
            EcoMindTheme { AchievementDetailContent(AchievementDetailUiState.NotFound, {}, {}, {}) }
        }
        compose.onNodeWithText(label(R.string.achievement_not_found)).assertIsDisplayed()
        compose.onNodeWithContentDescription(label(R.string.achievements_back)).assertHasClickAction()
    }

    @Test
    fun detailLoadsTheRequestedMedalThroughItsOwnUseCaseAndViewModel() {
        val collection = loaded().collection!!
        val repository = object : AchievementRepository {
            override val isSimulated = true
            override suspend fun getCatalog() = Result.success(collection.entries.map { it.achievement })
            override suspend fun getMyAwards() = Result.success(collection.entries.mapNotNull { it.award })
        }
        val sessions = object : SessionRepository {
            private val session = MutableStateFlow<Session?>(Session(1, "test@example.com", "demo", Long.MAX_VALUE))
            override fun getSession() = session
            override suspend fun saveSession(value: Session) { session.value = value }
            override suspend fun clearSession() { session.value = null }
        }
        val viewModel = AchievementDetailViewModel(GetAchievementByIdUseCase(GetAchievementsUseCase(repository, sessions)))
        compose.setContent {
            EcoMindTheme { AchievementDetailScreen("earned", {}, {}, viewModel) }
        }
        compose.onNodeWithText("First medal").assertIsDisplayed()
        compose.onNodeWithText("Next medal").assertDoesNotExist()
        compose.onNodeWithText("2026-10-08T15:00:00Z").assertIsDisplayed()
        compose.onNodeWithText(label(R.string.achievements_demo)).assertIsDisplayed()
    }

    private fun label(resource: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)

    private fun loaded(): AchievementsUiState {
        val earned = Achievement("earned", "TEST", "First medal", "Eligible action", "INDIVIDUAL", "ECOPOINTS", 10, true)
        val pending = earned.copy(id = "pending", name = "Next medal")
        val award = AchievementAward("award", "earned", "INDIVIDUAL", 1, "event", "2026-10-08T15:00:00Z", null)
        return AchievementsUiState(isLoading = false,
            collection = AchievementCollection(listOf(AchievementEntry(earned, award), AchievementEntry(pending, null)), true))
    }
}
