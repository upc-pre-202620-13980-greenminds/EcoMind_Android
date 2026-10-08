package pe.greenminds.ecomind.community.interfaces.events

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import pe.greenminds.ecomind.shared.interfaces.components.ComingSoonDialog
import pe.greenminds.ecomind.shared.interfaces.theme.Black
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.FadedGray
import pe.greenminds.ecomind.shared.interfaces.theme.HintGray
import pe.greenminds.ecomind.shared.interfaces.theme.MossGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SageGreen
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun CommunityScreen(viewModel: CommunityViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    CommunityContent(state = state)
}

@Composable
private fun CommunityContent(
    state: CommunityUiState,
    modifier: Modifier = Modifier
) {
    // Everything that is drawn but not built yet opens the same notice
    var showComingSoon by rememberSaveable { mutableStateOf(false) }
    val openComingSoon = { showComingSoon = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CommunitySelector(onOtherCommunities = openComingSoon)
            CommunityHeader()
            GoalCard(onClick = openComingSoon)
        }

        Spacer(modifier = Modifier.height(16.dp))
        CommunityTabs(onOtherTab = openComingSoon)

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            SearchRow(onAction = openComingSoon)

            if (state.isLoading) {
                val loadingDescription = stringResource(R.string.loading)
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                        .semantics { contentDescription = loadingDescription }
                )
            } else {
                state.events.forEach { event ->
                    CommunityEventCard(event = event, onAction = openComingSoon)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showComingSoon) {
        ComingSoonDialog(onDismiss = { showComingSoon = false })
    }
}

// "Local Community" is the one on screen; the other option is not built yet
@Composable
private fun CommunitySelector(onOtherCommunities: () -> Unit) {
    val containerShape = RoundedCornerShape(19.dp)
    val activeShape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(elevation = 3.dp, shape = containerShape)
            .background(White, containerShape)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .semantics { selected = true }
        ) {
            Text(
                text = stringResource(R.string.community_local),
                style = poppinsTextStyle(14, FontWeight.SemiBold),
                color = White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary, activeShape)
                    .padding(vertical = 4.dp)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(activeShape)
                .clickable(role = Role.Tab, onClick = onOtherCommunities)
        ) {
            Text(
                text = stringResource(R.string.community_more),
                style = poppinsTextStyle(14, FontWeight.SemiBold),
                color = SageGreen,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// PROVISIONAL: the web services have no route for the community or its goal yet,
// so these texts are fixed resources instead of data from a repository
@Composable
private fun CommunityHeader() {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = stringResource(R.string.community_sample_name),
            style = poppinsTextStyle(24, FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.community_sample_manager),
            style = poppinsTextStyle(13, FontWeight.Light),
            color = Black
        )
        Text(
            text = stringResource(R.string.community_sample_members),
            style = poppinsTextStyle(10, FontWeight.Light),
            color = Black
        )
    }
}

// Collapsed card of the community goal; expanding it is not built yet
@Composable
private fun GoalCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(elevation = 3.dp, shape = shape)
            .background(White, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.community_sample_goal),
            style = interTextStyle(16, FontWeight.Bold),
            color = EcoGreenDark,
            modifier = Modifier.weight(1f)
        )
        // The "back" arrow turned a quarter points down; it is decorative
        Icon(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = null,
            tint = EcoGreenDark,
            modifier = Modifier
                .size(24.dp)
                .rotate(-90f)
        )
    }
}

// Only "Events" has content; the other two tabs open the notice
@Composable
private fun CommunityTabs(onOtherTab: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        CommunityTab(
            text = stringResource(R.string.community_tab_awards),
            selected = false,
            onClick = onOtherTab,
            modifier = Modifier.weight(1f)
        )
        CommunityTab(
            text = stringResource(R.string.community_tab_events),
            selected = true,
            onClick = {},
            modifier = Modifier.weight(1f)
        )
        CommunityTab(
            text = stringResource(R.string.community_tab_news),
            selected = false,
            onClick = onOtherTab,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CommunityTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor: Color = if (selected) MossGreen else FadedGray
    val lineColor: Color = if (selected) MaterialTheme.colorScheme.primary else HintGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = text,
                style = interTextStyle(16, FontWeight.Bold),
                color = textColor
            )
        }
        // Line under the tabs; green under the selected one
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(lineColor)
        )
    }
}

// The search field and the two buttons are drawn as in the design but not built yet
@Composable
private fun SearchRow(onAction: () -> Unit) {
    val pillShape = RoundedCornerShape(percent = 50)
    val searchLabel = stringResource(R.string.community_search)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(pillShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(role = Role.Button, onClickLabel = searchLabel, onClick = onAction)
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = null
            )
            Text(
                text = searchLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            SmallActionPill(
                text = stringResource(R.string.community_create_event),
                onClick = onAction
            )
            SmallActionPill(
                text = stringResource(R.string.community_my_events),
                onClick = onAction
            )
        }
    }
}

// The pill is small as in the design; the box keeps a touch target of 48dp
@Composable
private fun SmallActionPill(text: String, onClick: () -> Unit) {
    val pillShape = RoundedCornerShape(percent = 50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(96.dp)
            .heightIn(min = 48.dp)
            .clip(pillShape)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Text(
            text = text,
            style = poppinsTextStyle(12),
            color = White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, pillShape)
                .padding(vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityContentPreview() {
    EcoMindTheme {
        CommunityContent(state = CommunityUiState(isLoading = false))
    }
}
