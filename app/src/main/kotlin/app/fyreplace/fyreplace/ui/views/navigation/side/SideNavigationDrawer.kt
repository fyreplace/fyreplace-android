package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text
import app.fyreplace.fyreplace.ui.views.navigation.contentPadding
import app.fyreplace.fyreplace.ui.views.navigation.isAt
import app.fyreplace.fyreplace.util.modify
import app.fyreplace.fyreplace.util.plus

@Composable
fun SideNavigationDrawer(
    navController: NavController,
    destinations: List<Destination>,
    windowPadding: PaddingValues,
    onClickDestination: (Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            val entry by navController.currentBackStackEntryAsState()
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(PaddingValues(12.dp) + windowPadding.modify(end = 0.dp))
            ) {
                for (destination in destinations) {
                    val selected = entry.isAt(destination)

                    NavigationDrawerItem(
                        selected = selected,
                        icon = { Icon(destination, selected = selected) },
                        label = { Text(destination) },
                        onClick = { onClickDestination(destination) },
                        modifier = Modifier.testTag("navigation:$destination")
                    )
                }
            }
        }
    ) {
        content(contentPadding(windowPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun SideNavigationDrawerPreview() {
    SideNavigationDrawer(
        navController = rememberNavController(),
        destinations = Destination.entries,
        windowPadding = PaddingValues(),
        onClickDestination = {},
        content = {}
    )
}
