package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text

@Composable
fun SideNavigationRail(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    isAuthenticated: Boolean,
    onClickDestination: (Destination.Singleton) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        for (destination in destinations) {
            val selected = destination == selectedDestination

            NavigationRailItem(
                selected = selected,
                enabled = !destination.requiresAuthentication || isAuthenticated,
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
        destinations = listOf(Destination.Feed, Destination.Notifications, Destination.Archive),
        selectedDestination = Destination.Feed,
        isAuthenticated = true,
        onClickDestination = {}
    )
}
