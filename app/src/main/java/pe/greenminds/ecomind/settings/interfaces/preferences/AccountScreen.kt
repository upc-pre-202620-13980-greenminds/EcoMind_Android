package pe.greenminds.ecomind.settings.interfaces.preferences

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R

@Composable
fun AccountScreen(onBack: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    SettingsPage(stringResource(R.string.settings_account_information), onBack) {
        when {
            state.loading -> CircularProgressIndicator()
            state.error -> { Text(stringResource(R.string.notifications_error)); TextButton(viewModel::load) { Text(stringResource(R.string.ranking_try_again)) } }
            else -> {
                SettingsHeading(stringResource(R.string.settings_name)); Text(state.profile?.name.orEmpty())
                SettingsHeading(stringResource(R.string.settings_email)); Text(state.email)
                SettingsHeading(stringResource(R.string.settings_account_id)); Text(state.profile?.id.toString())
                SettingsHeading(stringResource(R.string.settings_about)); Text(stringResource(R.string.settings_account_demo))
            }
        }
    }
}
