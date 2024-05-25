package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowWidthSizeClass
import app.fyreplace.fyreplace.ui.views.navigation.side.SideNavigationDrawer
import app.fyreplace.fyreplace.ui.views.navigation.side.SideNavigationRail
import app.fyreplace.fyreplace.util.modify

@Composable
fun SideNavigation(
    navController: NavController,
    destinations: List<Destination>,
    windowPadding: PaddingValues,
    onClickDestination: (Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (sizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        SideNavigationDrawer(
            navController = navController,
            destinations = destinations,
            windowPadding = windowPadding,
            onClickDestination = onClickDestination,
            content = content
        )
    } else {
        Row(modifier = Modifier.padding(windowPadding.modify(end = 0.dp))) {
            SideNavigationRail(
                navController = navController,
                destinations = destinations,
                onClickDestination = onClickDestination
            )
            content(contentPadding(windowPadding))
        }
    }
}

@Composable
fun contentPadding(windowPadding: PaddingValues) = windowPadding.modify(start = 0.dp, top = 0.dp)
