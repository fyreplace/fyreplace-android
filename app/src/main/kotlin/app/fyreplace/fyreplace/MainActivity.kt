package app.fyreplace.fyreplace

import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import app.fyreplace.fyreplace.input.DestinationKeyboardShortcut
import app.fyreplace.fyreplace.input.getShortcut
import app.fyreplace.fyreplace.input.keyboardShortcuts
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.bars.TopBar
import app.fyreplace.fyreplace.ui.views.navigation.BottomNavigation
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.SideNavigation
import app.fyreplace.fyreplace.ui.views.navigation.asDestination
import app.fyreplace.fyreplace.ui.views.navigation.sail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : SecureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statusColor = getColor(R.color.status)
        val navigationColor = getColor(R.color.navigation)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(statusColor, statusColor),
            navigationBarStyle = SystemBarStyle.auto(navigationColor, navigationColor),
        )
        setContent {
            AppTheme {
                MainContent()
            }
        }
    }

    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>,
        menu: Menu?,
        deviceId: Int
    ) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId)
        data.add(
            KeyboardShortcutGroup(
                getString(R.string.keyboard_shortcuts_navigation),
                keyboardShortcuts.map { it.getInfo(this) }
            )
        )
    }
}

@Composable
fun MainContent() {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val compactWidth = sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val compactHeight = sizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    val compact = compactWidth || compactHeight
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry.asDestination()
    val destinationSets = Destination.Set.topLevel(flatten = !compact)
    val selectedDestinationSet =
        destinationSets.find { (it.choices + it.root).contains(currentDestination) }
    val savedDestinations = rememberSaveable(saver = destinationMapSaver()) { mutableStateMapOf() }

    fun onClickDestination(destination: Destination) {
        val actualDestination = destinationSets.find { it.root == destination }
            ?.choices
            ?.firstOrNull()
            ?: destination
        navController.sail(savedDestinations[actualDestination] ?: actualDestination)
    }

    val keyboardHandler = Modifier.onKeyEvent { event ->
        when (event.type) {
            KeyEventType.KeyUp -> {
                val shortcut = getShortcut(event)

                if (shortcut is DestinationKeyboardShortcut) {
                    onClickDestination(shortcut.destination)
                    return@onKeyEvent true
                }
            }
        }

        return@onKeyEvent false
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun Host(innerPadding: PaddingValues, modifier: Modifier = Modifier) = SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Destination.entries.forEach { destination ->
                composable(destination.route) {
                    destination.content(
                        Destination.ContentInfo(
                            transitionScope = this@SharedTransitionLayout,
                            visibilityScope = this
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun Top() {
        TopBar(
            destinations = selectedDestinationSet?.choices ?: emptyList(),
            selectedDestination = currentDestination,
            onClickDestination = {
                navController.sail(it)

                if (selectedDestinationSet?.defaultDestination != null) {
                    savedDestinations[selectedDestinationSet.defaultDestination] = it
                }
            }
        )
    }

    if (compact) {
        Scaffold(
            topBar = {
                Top()
            },
            bottomBar = {
                BottomNavigation(
                    destinations = destinationSets.map(Destination.Set::root),
                    selectedDestination = selectedDestinationSet?.root,
                    onClickDestination = ::onClickDestination
                )
            }
        ) {
            Host(it, modifier = keyboardHandler)
        }
    } else {
        Scaffold {
            SideNavigation(
                destinations = destinationSets.map(Destination.Set::root),
                selectedDestination = selectedDestinationSet?.root,
                windowPadding = it,
                onClickDestination = ::onClickDestination,
                modifier = keyboardHandler
            ) { contentPadding ->
                Column {
                    Top()
                    Host(contentPadding)
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MainContentPreview() {
    AppTheme {
        MainContent()
    }
}

private fun destinationMapSaver() = mapSaver(
    save = {
        mutableMapOf<String, Any>().apply {
            it.forEach { (key, value) -> put(key.route, value) }
        }
    },
    restore = {
        mutableMapOf<Destination, Destination>().apply {
            it.forEach { (key, value) ->
                put(
                    requireNotNull(Destination.byRoute(key)),
                    value as Destination
                )
            }
        }
    }
)
