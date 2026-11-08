package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.community.domain.entity.Community

@Composable
fun CommunityDetailsDialog(
    community: Community,
    isJoining: Boolean,
    errorMessage: String?,
    onJoin: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isJoining) onCancel() },
        title = { Text(community.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                community.locality?.let { Text("Locality: $it") }
                community.memberLimit?.let { Text("Member limit: $it") }
                Text(community.description.orEmpty())
                errorMessage?.let { Text(it) }

                if (isJoining) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onJoin, enabled = !isJoining) {
                Text(if (isJoining) "Joining…" else "Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !isJoining) {
                Text("Cancel")
            }
        }
    )
}
