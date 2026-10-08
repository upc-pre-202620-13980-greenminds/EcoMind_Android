package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import pe.greenminds.ecomind.shared.interfaces.components.ComingSoonDialog
import pe.greenminds.ecomind.shared.interfaces.theme.DarkGrayText
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.FieldGray
import pe.greenminds.ecomind.shared.interfaces.theme.MutedGray
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun StoreScreen(viewModel: StoreViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    StoreContent(state = state)
}

@Composable
private fun StoreContent(
    state: StoreUiState,
    modifier: Modifier = Modifier
) {
    // Everything that is drawn but not built yet opens the same notice
    var showComingSoon by rememberSaveable { mutableStateOf(false) }
    val openComingSoon = { showComingSoon = true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.store_title),
            style = poppinsTextStyle(36, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = stringResource(R.string.store_subtitle),
            style = poppinsTextStyle(10, FontWeight.Bold),
            color = MutedGray,
            textAlign = TextAlign.Center
        )

        // Main tabs: only the first one has content; the others open the notice
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StoreTab(
                text = stringResource(R.string.store_tab_cosmetics),
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            StoreTab(
                text = stringResource(R.string.store_tab_multipliers),
                selected = false,
                onClick = openComingSoon,
                modifier = Modifier.weight(1f)
            )
            StoreTab(
                text = stringResource(R.string.store_tab_gems),
                selected = false,
                onClick = openComingSoon,
                modifier = Modifier.weight(1f)
            )
        }

        // Secondary tabs: catalog or what the user owns
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            StoreTab(
                text = stringResource(R.string.store_subtab_store),
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            StoreTab(
                text = stringResource(R.string.store_subtab_inventory),
                selected = false,
                onClick = openComingSoon,
                outlined = true,
                modifier = Modifier.weight(1f)
            )
            // Empty third column, so the two tabs keep the width they have in the design
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = stringResource(R.string.store_section_cosmetics),
            style = poppinsTextStyle(14, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp)
                .semantics { heading() }
        )

        if (state.isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            StoreGrid(items = state.items, onAction = openComingSoon)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showComingSoon) {
        ComingSoonDialog(onDismiss = { showComingSoon = false })
    }
}

// Two cards per row. The list is short, so rows are built by hand instead of a lazy grid,
// which cannot be placed inside a column that scrolls.
@Composable
private fun StoreGrid(
    items: List<StoreItem>,
    onAction: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                rowItems.forEach { item ->
                    StoreItemCard(
                        item = item,
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Keeps the width of a single card when the last row has only one
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Pill used by both rows of tabs: filled green when selected, gray or outlined otherwise
@Composable
private fun StoreTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp)
    val background: Color = when {
        selected -> MaterialTheme.colorScheme.primary
        outlined -> White
        else -> FieldGray
    }

    // The outer box keeps a touch target of 48dp; the pill inside has the size of the design
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
    ) {
        Text(
            text = text,
            style = poppinsTextStyle(10, FontWeight.SemiBold),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else DarkGrayText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(background, shape)
                .then(if (outlined && !selected) Modifier.border(1.dp, FieldGray, shape) else Modifier)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun StoreContentPreview() {
    EcoMindTheme {
        StoreContent(state = StoreUiState(isLoading = false))
    }
}
