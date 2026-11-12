package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun CommunityNewsContent(
    posts: List<CommunityPost>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            posts.isEmpty() -> Text(stringResource(R.string.community_no_posts))

            else -> posts.forEach { post ->
                CommunityPostCard(post)
            }
        }
    }
}

@Composable
private fun CommunityPostCard(post: CommunityPost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(28.dp))
            .border(2.dp, CardBorder, RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = post.content,
            style = interTextStyle(14),
            color = TextPrimary
        )

        post.imageUrl?.takeIf(String::isNotBlank)?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = stringResource(R.string.community_likes_count, post.likes),
            style = interTextStyle(12),
            color = TextPrimary
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityNewsContentPreview() {
    EcoMindTheme {
        CommunityNewsContent(
            posts = emptyList(),
            isLoading = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}