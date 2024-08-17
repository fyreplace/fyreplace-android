package app.fyreplace.fyreplace.ui.views.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import com.google.protobuf.ByteString

@Composable
fun BottomNavigation(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    onClickDestination: (Destination.Singleton) -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(requireNotNull(activity))
) = NavigationBar {
    val isAuthenticated by mainViewModel.isAuthenticated.collectAsStateWithLifecycle()

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
        onClickDestination = {},
        mainViewModel = MainViewModel(
            state = SavedStateHandle(),
            eventBus = FakeEventBus(),
            storeResolver = FakeStoreResolver(
                secrets = Secrets.newBuilder()
                    .setToken(ByteString.copyFromUtf8("token:iv"))
                    .build()
            )
        )
    )
}
