package pe.greenminds.ecomind.quests

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.application.QuestFilters
import pe.greenminds.ecomind.quests.interfaces.search.*
import pe.greenminds.ecomind.main.*
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import java.io.File

class QuestFiltersScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun opensFiltersAppliesSelectionsAndRestoresThem() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var showing by mutableStateOf(false)
        var applied by mutableStateOf(QuestFilters())
        compose.setContent {
            EcoMindTheme {
                Scaffold(topBar = { EcoMindTopBar(360, 16, {}, {}) }, bottomBar = { MainNavigationBar(NavigationItem.MAIN_MENU, {}) }) { padding ->
                    QuestSearchContent(QuestSearchUiState(isLoading = false, filters = applied), {}, {}, {},
                        onOpenFilters = { showing = !showing }, showFilters = showing,
                        onApplyFilters = { applied = it; showing = false }, modifier = Modifier.padding(padding))
                }
            }
        }
        compose.onNodeWithContentDescription(context.getString(R.string.quest_open_filters)).performClick()
        compose.onNodeWithText(context.getString(R.string.quest_filter_type)).assertIsDisplayed()
        File(context.getExternalFilesDir(null), "quest-filters.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(context.getString(R.string.quest_theme_collaborative).replaceFirstChar(Char::uppercase)).performClick()
        compose.onNodeWithText(context.getString(R.string.quest_filter_category)).performClick()
        compose.onNodeWithText(context.getString(R.string.quest_category_energy).replaceFirstChar(Char::uppercase)).performClick()
        compose.onNodeWithText(context.getString(R.string.quest_filter_apply)).performClick()
        compose.runOnIdle { assertEquals(setOf("COLLABORATIVE"), applied.types); assertEquals(setOf("ENERGY"), applied.categories) }
        compose.onNodeWithContentDescription(context.getString(R.string.quest_open_filters)).performClick()
        compose.onNodeWithText(context.getString(R.string.quest_theme_collaborative).replaceFirstChar(Char::uppercase)).assertIsOn()
    }
}
