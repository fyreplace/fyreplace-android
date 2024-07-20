package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import app.fyreplace.fyreplace.extensions.modify
import app.fyreplace.fyreplace.ui.views.navigation.side.SideNavigationDrawer
import app.fyreplace.fyreplace.ui.views.navigation.side.SideNavigationRail

@Composable
fun SideNavigation(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    windowPadding: PaddingValues,
    onClickDestination: (Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (sizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        SideNavigationDrawer(
            destinations = destinations,
            selectedDestination = selectedDestination,
            windowPadding = windowPadding,
            onClickDestination = onClickDestination,
            content = content
        )
    } else {
        Row {
            SideNavigationRail(
                destinations = destinations,
                selectedDestination = selectedDestination,
                onClickDestination = onClickDestination
            )
            content(contentPadding(windowPadding))
        }
    }
}

@Composable
fun contentPadding(windowPadding: PaddingValues) = windowPadding.modify(start = 0.dp, top = 0.dp)
