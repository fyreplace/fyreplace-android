package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text
import app.fyreplace.fyreplace.ui.views.navigation.isAt

@Composable
fun SideNavigationRail(
    navController: NavController,
    destinations: List<Destination>,
    onClickDestination: (Destination) -> Unit
) {
    val entry by navController.currentBackStackEntryAsState()
    NavigationRail {
        for (destination in destinations) {
            val selected = entry.isAt(destination)

            NavigationRailItem(
                selected = selected,
                icon = { Icon(destination, selected = selected) },
                label = { Text(destination) },
                onClick = { onClickDestination(destination) },
                modifier = Modifier.testTag("navigation:$destination")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideNavigationRailPreview() {
    SideNavigationRail(
        navController = rememberNavController(),
        destinations = Destination.entries,
        onClickDestination = {}
    )
}
