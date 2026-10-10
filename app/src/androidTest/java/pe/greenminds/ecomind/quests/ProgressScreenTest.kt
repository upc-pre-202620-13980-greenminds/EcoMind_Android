package pe.greenminds.ecomind.quests

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.main.MainNavigationBar
import pe.greenminds.ecomind.main.NavigationItem
import pe.greenminds.ecomind.quests.application.usecase.GetProgressOverviewUseCase
import pe.greenminds.ecomind.quests.infrastructure.implementation.LocalQuestProgressRepository
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import pe.greenminds.ecomind.quests.presentation.ui.progress.ProgressContent
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import java.io.File

class ProgressScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun restoresSectionsAndKeepsPendingActionsAsDialogs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val overview = runBlocking {
            GetProgressOverviewUseCase(LocalQuestProgressRepository())().getOrThrow()
        }
        compose.setContent {
            EcoMindTheme {
                Scaffold(
                    topBar = { EcoMindTopBar(50, 200, {}, {}) },
                    bottomBar = { MainNavigationBar(NavigationItem.PROGRESS, {}) }
                ) { padding ->
                    ProgressContent(
                        state = ProgressUiState(
                            isLoading = false, inProgress = overview.inProgress,
                            familyQuests = overview.familyQuests, finished = overview.finished
                        ),
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
        listOf(R.string.progress_in_progress, R.string.progress_family_quest, R.string.progress_finished)
            .forEach { compose.onNodeWithText(context.getString(it)).assertIsDisplayed() }
        compose.onNodeWithContentDescription(
            context.getString(R.string.progress_entry_description, "Daily Quest", 70)
        ).assertIsDisplayed()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), "progress-restored.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(context.getString(R.string.progress_see_more)).performClick()
        compose.onNodeWithText(context.getString(R.string.coming_soon_title)).assertIsDisplayed()
    }
}
