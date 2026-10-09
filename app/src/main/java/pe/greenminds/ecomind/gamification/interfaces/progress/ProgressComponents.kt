package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.interfaces.achievements.*
import pe.greenminds.ecomind.shared.interfaces.components.SegmentedTabs
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
internal fun <T> ColumnScope.LoadedContent(state: LoadState<T>, retry: () -> Unit, signIn: () -> Unit,
    content: @Composable ColumnScope.(T) -> Unit) {
    when (state) {
        LoadState.Loading -> LoadingContent()
        is LoadState.Failed -> RecoveryContent(state.sessionRequired, retry, signIn)
        is LoadState.Loaded -> content(state.value)
    }
}
