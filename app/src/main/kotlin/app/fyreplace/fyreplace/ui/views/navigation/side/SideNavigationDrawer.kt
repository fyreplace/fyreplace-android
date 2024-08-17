package app.fyreplace.fyreplace.ui.views.navigation.side

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.extensions.modify
import app.fyreplace.fyreplace.extensions.plus
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.Text
import app.fyreplace.fyreplace.ui.views.navigation.contentPadding
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import com.google.protobuf.ByteString

@Composable
fun SideNavigationDrawer(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    windowPadding: PaddingValues,
    onClickDestination: (Destination.Singleton) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(requireNotNull(activity)),
    content: @Composable (PaddingValues) -> Unit
) = PermanentNavigationDrawer(
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

            val isAuthenticated by mainViewModel.isAuthenticated.collectAsStateWithLifecycle()

            for (destination in destinations) {
                DrawerItem(
                    destination = destination,
                    selected = destination == selectedDestination,
                    enabled = !destination.requiresAuthentication || isAuthenticated,
                    onClickDestination = onClickDestination
                )
            }
        }
    },
    modifier = modifier
) {
    content(contentPadding(windowPadding))
}

@Composable
private fun DrawerItem(
    destination: Destination.Singleton,
    selected: Boolean,
    enabled: Boolean,
    onClickDestination: (Destination.Singleton) -> Unit
) = if (enabled) {
    NavigationDrawerItem(
        selected = selected,
        icon = { Icon(destination, active = selected) },
        label = { Text(destination) },
        onClick = { onClickDestination(destination) }
    )
} else {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.Gray) {
            Icon(destination, active = false)
            Spacer(modifier = Modifier.width(12.dp))
            Text(destination)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SideNavigationDrawerPreview() {
    SideNavigationDrawer(
        destinations = listOf(Destination.Feed, Destination.Notifications, Destination.Archive),
        selectedDestination = Destination.Feed,
        windowPadding = PaddingValues(),
        onClickDestination = {},
        mainViewModel = MainViewModel(
            state = SavedStateHandle(),
            eventBus = FakeEventBus(),
            storeResolver = FakeStoreResolver(
                secrets = Secrets.newBuilder()
                    .setToken(ByteString.copyFromUtf8("token:iv"))
                    .build()
            )
        ),
        content = {}
    )
}
