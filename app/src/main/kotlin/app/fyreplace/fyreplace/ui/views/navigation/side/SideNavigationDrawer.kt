package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.modify
import app.fyreplace.fyreplace.extensions.plus
import app.fyreplace.fyreplace.ui.topLevelDestinationGroups
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text
import app.fyreplace.fyreplace.ui.views.navigation.contentPadding

@Composable
fun SideNavigationDrawer(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    windowPadding: PaddingValues,
    onClickDestination: (Destination.Singleton) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(PaddingValues(12.dp) + windowPadding.modify(end = 0.dp))
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
                )

                for (destination in destinations) {
                    val selected = destination == selectedDestination

                    NavigationDrawerItem(
                        selected = selected,
                        icon = { Icon(destination, active = selected) },
                        label = { Text(destination) },
                        onClick = { onClickDestination(destination) }
                    )
                }
            }
        },
        modifier = modifier
    ) {
        content(contentPadding(windowPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun SideNavigationDrawerPreview() {
    SideNavigationDrawer(
        destinations = topLevelDestinationGroups(expanded = true, userAuthenticated = false)
            .map(Destination.Singleton.Group::root),
        selectedDestination = Destination.Feed,
        windowPadding = PaddingValues(),
        onClickDestination = {},
        content = {}
    )
}
