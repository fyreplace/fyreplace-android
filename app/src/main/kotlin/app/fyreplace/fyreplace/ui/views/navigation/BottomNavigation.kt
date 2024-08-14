package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.ui.topLevelDestinationGroups

@Composable
fun BottomNavigation(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    onClickDestination: (Destination.Singleton) -> Unit
) {
    NavigationBar {
        for (destination in destinations) {
            val selected = destination == selectedDestination

            NavigationBarItem(
                selected = selected,
                icon = { Icon(destination, active = selected) },
                label = { Text(destination) },
                onClick = { onClickDestination(destination) }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationPreview() {
    BottomNavigation(
        destinations = topLevelDestinationGroups(expanded = false, userAuthenticated = false)
            .map(Destination.Singleton.Group::root),
        selectedDestination = Destination.Feed,
        onClickDestination = {}
    )
}
