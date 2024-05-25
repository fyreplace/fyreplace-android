package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun BottomNavigation(
    navController: NavController,
    destinations: List<Destination>,
    onClickDestination: (Destination) -> Unit
) {
    NavigationBar {
        val entry by navController.currentBackStackEntryAsState()

        for (destination in destinations) {
            val selected = entry.isAt(destination, exactly = false)

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
        navController = rememberNavController(),
        destinations = Destination.essentials,
        onClickDestination = {}
    )
}
