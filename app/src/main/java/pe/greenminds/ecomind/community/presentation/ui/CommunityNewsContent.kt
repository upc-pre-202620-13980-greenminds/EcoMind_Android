package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.presentation.viewmodel.CommunityNewsViewModel
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun CommunityNewsContent(
    communityId: Long,
    modifier: Modifier = Modifier,
    viewModel: CommunityNewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(communityId, viewModel) {
        viewModel.bindCommunity(communityId)
        onDispose {
            viewModel.clear()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val error = state.error
        ?: state.errorRes?.let { stringResource(it) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = viewModel::refresh,
            enabled = !state.isLoading && !state.isSavingReaction,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.news_refresh))
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!state.isLoading && error == null && state.posts.isEmpty()) {
            Text(stringResource(R.string.community_no_posts))
        }

        if (state.communityId == communityId) {
            state.posts.forEach { post ->
                key(post.id) {
                    CommunityPostCard(
                        post = post,
                        onChooseReaction = {
                            viewModel.openReactions(post.id, picker = true)
                        },
                        onShowReactions = {
                            viewModel.openReactions(post.id, picker = false)
                        }
                    )
                }
            }
        }
    }

    if (
        state.communityId == communityId &&
        state.selectedPostId != null
    ) {
        PostReactionsDialog(
            state = state,
            onSelect = viewModel::chooseReaction,
            onRemove = viewModel::removeOwnReaction,
            onRetry = viewModel::retryReactions,
            onDismiss = viewModel::closeDialog
        )
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    onChooseReaction: () -> Unit,
    onShowReactions: () -> Unit
) {
    val authorName = post.authorName?.takeIf { it.isNotBlank() }
        ?: stringResource(
            if (post.authorId == null) {
                R.string.news_system_author
            } else {
                R.string.news_unknown_author
            }
        )

    val publishedAt = post.createdAt?.let {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .format(it.atZone(ZoneId.systemDefault()))
    } ?: stringResource(R.string.news_date_unavailable)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(40.dp))
            .border(3.dp, CardBorder, RoundedCornerShape(40.dp))
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PostAuthorAvatar(
                name = authorName,
                reference = post.authorAvatar
            )

            Text(
                text = authorName,
                style = interTextStyle(18, FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = post.content,
            style = interTextStyle(14),
            color = TextPrimary
        )

        post.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = publishedAt,
                style = interTextStyle(11),
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onChooseReaction) {
                Image(
                    painter = painterResource(R.drawable.add_reaction),
                    contentDescription = stringResource(R.string.news_choose_reaction),
                    modifier = Modifier.size(22.dp)
                )
            }

            TextButton(
                onClick = onShowReactions,
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.news_reactions_count,
                        post.likes
                    ),
                    style = interTextStyle(12),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PostAuthorAvatar(
    name: String,
    reference: String?
) {
    val localImage = when (reference) {
        "avatar_alex" -> R.drawable.avatar_alex
        "avatar_leaf", "avatar_leafwings" -> R.drawable.avatar_leafwings
        "avatar_lunalunette" -> R.drawable.avatar_lunalunette
        "avatar_mickey" -> R.drawable.avatar_mickey
        "avatar_ranma" -> R.drawable.avatar_ranma
        "avatar_rosalina" -> R.drawable.avatar_rosalina
        "avatar_salsolcito" -> R.drawable.avatar_salsolcito
        "avatar_sonic" -> R.drawable.avatar_sonic
        else -> null
    }

    val modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .border(1.dp, CardBorder, CircleShape)

    when {
        localImage != null -> {
            Image(
                painter = painterResource(localImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }

        reference?.startsWith("https://") == true ||
                reference?.startsWith("http://") == true -> {
            Box(modifier = modifier) {
                InitialsAvatar(name = name)
                AsyncImage(
                    model = reference,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        else -> {
            InitialsAvatar(
                name = name,
                modifier = modifier
            )
        }
    }
}