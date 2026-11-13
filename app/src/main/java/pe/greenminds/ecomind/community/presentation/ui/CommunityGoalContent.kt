package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.presentation.viewmodel.CommunityGoalViewModel

@Composable
fun CommunityGoalContent(
    communityId: Long,
    modifier: Modifier = Modifier,
    viewModel: CommunityGoalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val panel = state.panel
    val goal = panel?.goal
    val green = Color(0xFF278449)
    val lightGreen = Color(0xFFE4F5DA)
    val busy = state.loading || state.saving

    DisposableEffect(communityId, viewModel) {
        viewModel.bind(communityId)
        onDispose { viewModel.clear() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val error = state.error ?: state.errorRes?.let { stringResource(it) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(lightGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable(onClick = viewModel::toggleExpanded)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        state.loading && panel == null ->
                            stringResource(R.string.community_loading_goal)

                        goal == null ->
                            stringResource(R.string.community_no_active_goal)

                        else -> stringResource(
                            R.string.goal_flow_title,
                            goal.target,
                            topicLabel(goal.topic)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    color = green,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_back),
                    contentDescription = stringResource(
                        if (state.expanded) {
                            R.string.goal_flow_collapse
                        } else {
                            R.string.goal_flow_expand
                        }
                    ),
                    tint = green,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (state.expanded) 90f else -90f)
                )
            }

            AnimatedVisibility(visible = state.expanded) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (busy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (goal != null) {
                        val fraction = if (goal.target > 0) {
                            (goal.progress.toFloat() / goal.target).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        Text(stringResource(R.string.goal_flow_progress))

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF76AD4C),
                            trackColor = Color(0xFFE6E6E6)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(25, 50, 75, 100).forEach { percent ->
                                val reached = fraction * 100 >= percent
                                val color = if (reached) {
                                    Color(0xFF6AA344)
                                } else {
                                    Color.Gray
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Text("$percent%", color = color)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.goal_flow_count,
                                    goal.progress,
                                    goal.target
                                ),
                                modifier = Modifier.weight(1f),
                                color = green,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = stringResource(
                                    R.string.goal_flow_participants,
                                    goal.participants
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        when {
                            panel.joined -> {
                                Text(
                                    stringResource(R.string.goal_flow_joined),
                                    color = green
                                )
                            }

                            panel.otherGoalActive -> {
                                Text(stringResource(R.string.goal_flow_other_active))
                            }

                            else -> {
                                Button(
                                    onClick = viewModel::join,
                                    enabled = !busy,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = green
                                    )
                                ) {
                                    Text(stringResource(R.string.goal_flow_join))
                                }
                            }
                        }
                    } else if (panel?.canCreate == true) {
                        if (panel.otherGoalActive) {
                            Text(stringResource(R.string.goal_flow_other_active))
                        }

                        Button(
                            onClick = viewModel::openCreate,
                            enabled = !busy && !panel.otherGoalActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = green
                            )
                        ) {
                            Text(stringResource(R.string.goal_flow_create))
                        }
                    }

                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }

                    TextButton(
                        onClick = viewModel::refresh,
                        enabled = !busy
                    ) {
                        Text(stringResource(R.string.goal_flow_refresh))
                    }
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.img_peeking),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-38).dp, y = (-25).dp)
                .width(85.dp)
                .height(30.dp)
        )
    }

    if (state.showCreate) {
        AlertDialog(
            onDismissRequest = viewModel::closeCreate,
            title = {
                Text(stringResource(R.string.goal_flow_create_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.goal_flow_choose_topic))

                    listOf("water", "energy", "recycle").forEach { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !busy) {
                                    viewModel.selectTopic(topic)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.topic == topic,
                                onClick = { viewModel.selectTopic(topic) },
                                enabled = !busy
                            )
                            Text(topicLabel(topic))
                        }
                    }

                    panel?.let {
                        Text(
                            stringResource(
                                R.string.goal_flow_target_preview,
                                it.memberCount,
                                it.targetPreview
                            )
                        )
                    }

                    Text(stringResource(R.string.goal_flow_auto_join))

                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }

                    if (busy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (panel == null && !busy) {
                        TextButton(onClick = viewModel::refresh) {
                            Text(stringResource(R.string.goal_flow_refresh))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::create,
                    enabled = !busy &&
                            panel?.canCreate == true &&
                            !panel.otherGoalActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green
                    )
                ) {
                    Text(stringResource(R.string.goal_flow_create))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::closeCreate,
                    enabled = !state.saving
                ) {
                    Text(stringResource(R.string.community_cancel))
                }
            }
        )
    }
}

@Composable
private fun topicLabel(topic: String): String =
    stringResource(
        when (topic.lowercase()) {
            "water" -> R.string.goal_flow_water
            "energy" -> R.string.goal_flow_energy
            else -> R.string.goal_flow_recycle
        }
    )