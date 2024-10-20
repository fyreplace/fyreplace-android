package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavigation(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    isAuthenticated: Boolean,
    onClickDestination: (Destination.Singleton) -> Unit
) = NavigationBar {
    for (destination in destinations) {
        val selected = destination == selectedDestination

        NavigationBarItem(
            selected = selected,
            enabled = !destination.requiresAuthentication || isAuthenticated,
            icon = { Icon(destination, active = selected) },
            label = { Text(destination) },
            onClick = { onClickDestination(destination) }
        )
    }
}

@Preview
@Composable
fun BottomNavigationPreview() {
    BottomNavigation(
        destinations = listOf(Destination.Feed, Destination.Notifications, Destination.Archive),
        selectedDestination = Destination.Feed,
        isAuthenticated = true,
        onClickDestination = {}
    )
}
