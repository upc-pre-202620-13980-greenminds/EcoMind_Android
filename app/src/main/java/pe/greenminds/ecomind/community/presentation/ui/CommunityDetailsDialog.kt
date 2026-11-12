package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.MossGreen
import pe.greenminds.ecomind.shared.interfaces.theme.White

private val CommunityDialogGreen = Color(0xFFE4F5DA)
private val CommunityDialogCancel = Color(0xFFD95858)

@Composable
fun CommunityDetailsDialog(
    community: Community,
    isJoining: Boolean,
    errorMessage: String?,
    onJoin: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isJoining) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(10.dp, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = CommunityDialogGreen
        ) {
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(66.dp)
                                .background(CommunityDialogGreen)
                        ) {
                            Text(
                                text = community.name,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth(0.76f)
                                    .padding(start = 14.dp, end = 8.dp),
                                fontSize = 18.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF202020),
                                maxLines = 2
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(White)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            community.memberLimit?.let { limit ->
                                CommunityDetailLine(label = stringResource(R.string.community_member_limit), value = limit.toString())
                            }
                            community.locality?.let { locality ->
                                CommunityDetailLine(label = stringResource(R.string.community_locality), value = locality)
                            }
                            CommunityDetailLine(
                                label = stringResource(R.string.community_description),
                                value = community.description?.takeIf(String::isNotBlank)
                                    ?: stringResource(R.string.community_no_description)
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = CommunityDialogCancel,
                                    fontSize = 12.sp
                                )
                            }

                            if (isJoining) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MossGreen,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }

                    CommunityIcon(
                        community = community,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-16).dp, y = 30.dp)
                            .size(62.dp)
                            .zIndex(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onJoin,
                    enabled = !isJoining,
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(40.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) {
                    Text(
                        text = stringResource(if (isJoining) R.string.community_joining else R.string.community_join),
                        color = White,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = onCancel,
                    enabled = !isJoining,
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(40.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CommunityDialogCancel,
                        disabledContainerColor = CommunityDialogCancel.copy(alpha = 0.55f)
                    )
                ) {
                    Text(stringResource(R.string.community_cancel), color = White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CommunityDetailLine(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202020)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF202020)
        )
    }
}

@Composable
internal fun CommunityIcon(
    community: Community,
    modifier: Modifier = Modifier
) {
    val imageUrl = community.iconUrl?.takeIf(String::isNotBlank)
    var imageLoadFailed by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(White)
            .border(2.dp, CardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null && !imageLoadFailed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.community_icon_content_description, community.name),
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onError = { imageLoadFailed = true }
            )
        } else {
            Text(
                text = community.name.firstOrNull()?.uppercase() ?: "C",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}
