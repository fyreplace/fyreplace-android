package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.ui.topLevelDestinationGroups
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text

@Composable
fun SideNavigationRail(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    onClickDestination: (Destination.Singleton) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        for (destination in destinations) {
            val selected = destination == selectedDestination

            NavigationRailItem(
                selected = selected,
                icon = { Icon(destination, active = selected) },
                label = { Text(destination) },
                onClick = { onClickDestination(destination) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideNavigationRailPreview() {
    SideNavigationRail(
        destinations = topLevelDestinationGroups(expanded = true, userAuthenticated = false)
            .map(Destination.Singleton.Group::root),
        selectedDestination = Destination.Feed,
        onClickDestination = {}
    )
}
