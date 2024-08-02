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
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onClickDestination: (Destination) -> Unit,
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
        destinations = Destination.Set.topLevel(flatten = true).map(Destination.Set::root),
        selectedDestination = Destination.FEED,
        onClickDestination = {}
    )
}
