package pe.greenminds.ecomind.shared.interfaces.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Frame 73
@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LanguageContent(
        selected = state.language,
        onSelect = viewModel::onLanguageSelected,
        onBack = onBack
    )
}

@Composable
private fun LanguageContent(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        PreferencesHeader(title = stringResource(R.string.settings_language), onBack = onBack)
        PreferencesDescription(stringResource(R.string.settings_language_description))

        // selectableGroup tells the screen reader that only one option can be chosen
        Column(modifier = Modifier.selectableGroup()) {
            PreferencesGroup {
                AppLanguage.entries.forEachIndexed { index, language ->
                    if (index > 0) PreferencesDivider()
                    RadioOptionRow(
                        label = stringResource(languageName(language)),
                        selected = language == selected,
                        onSelect = { onSelect(language) }
                    )
                }
            }
        }
    }
}

// Each language is written in that language, so these names are not translated
fun languageName(language: AppLanguage): Int = when (language) {
    AppLanguage.ENGLISH -> R.string.settings_language_english
    AppLanguage.SPANISH -> R.string.settings_language_spanish
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LanguagePreview() {
    EcoMindTheme {
        LanguageContent(selected = AppLanguage.ENGLISH, onSelect = {}, onBack = {})
    }
}
