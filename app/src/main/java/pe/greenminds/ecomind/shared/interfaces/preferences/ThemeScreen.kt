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
import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Frame 74
@Composable
fun ThemeScreen(
    onBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    ThemeContent(
        selected = state.theme,
        saveError = state.saveError?.let { stringResource(it) },
        onSelect = viewModel::onThemeSelected,
        onBack = onBack
    )
}

@Composable
private fun ThemeContent(
    selected: AppTheme,
    saveError: String?,
    onSelect: (AppTheme) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        PreferencesHeader(title = stringResource(R.string.settings_theme), onBack = onBack)
        PreferencesDescription(stringResource(R.string.settings_theme_description))

        Column(modifier = Modifier.selectableGroup()) {
            PreferencesGroup {
                AppTheme.entries.forEachIndexed { index, theme ->
                    if (index > 0) PreferencesDivider()
                    RadioOptionRow(
                        label = stringResource(themeName(theme)),
                        selected = theme == selected,
                        onSelect = { onSelect(theme) }
                    )
                }
            }
        }
        if (saveError != null) PreferencesSaveError(saveError)
    }
}

fun themeName(theme: AppTheme): Int = when (theme) {
    AppTheme.LIGHT -> R.string.settings_theme_light
    AppTheme.DARK -> R.string.settings_theme_dark
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ThemePreview() {
    EcoMindTheme {
        ThemeContent(selected = AppTheme.LIGHT, saveError = null, onSelect = {}, onBack = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ThemeDarkPreview() {
    EcoMindTheme(darkTheme = true) {
        ThemeContent(selected = AppTheme.DARK, saveError = null, onSelect = {}, onBack = {})
    }
}
