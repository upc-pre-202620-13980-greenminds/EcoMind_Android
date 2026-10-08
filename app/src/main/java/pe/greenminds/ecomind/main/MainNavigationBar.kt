package pe.greenminds.ecomind.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun MainNavigationBar(
    selectedItem: NavigationItem,
    onItemClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .selectableGroup()
        ) {
            NavigationItem.entries.forEach { item ->
                val selected = item == selectedItem

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        // Each icon keeps a touch target of at least 48dp
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onItemClick(item) }
                        )
                ) {
                    // The inactive images already come faded from the design
                    Image(
                        painter = painterResource(
                            if (selected) item.activeIcon else item.inactiveIcon
                        ),
                        contentDescription = stringResource(item.label)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MainNavigationBarPreview() {
    EcoMindTheme {
        MainNavigationBar(
            selectedItem = NavigationItem.MAIN_MENU,
            onItemClick = {}
        )
    }
}
