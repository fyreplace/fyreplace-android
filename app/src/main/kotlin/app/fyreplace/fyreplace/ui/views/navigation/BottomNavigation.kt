package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavigation(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onClickDestination: (Destination) -> Unit
) {
    NavigationBar {
        for (destination in destinations) {
            val selected = destination == selectedDestination

            NavigationBarItem(
                selected = selected,
                icon = { Icon(destination, selected = selected) },
                label = { Text(destination) },
                onClick = { onClickDestination(destination) },
                modifier = Modifier.testTag("navigation:$destination")
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationPreview() {
    BottomNavigation(
        destinations = Destination.Set.topLevel(flatten = false).map(Destination.Set::root),
        selectedDestination = Destination.FEED,
        onClickDestination = {}
    )
}
