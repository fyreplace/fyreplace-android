package app.fyreplace.fyreplace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.bars.TopBar
import app.fyreplace.fyreplace.ui.views.navigation.BottomNavigation
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.SideNavigation
import app.fyreplace.fyreplace.ui.views.navigation.asDestination
import app.fyreplace.fyreplace.ui.views.navigation.composable
import app.fyreplace.fyreplace.ui.views.navigation.sail

class MainActivity : ComponentActivity() {
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
}

@Composable
fun MainContent() {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val compactWidth = sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val compactHeight = sizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    val navController = rememberNavController()

    @Composable
    fun Host(innerPadding: PaddingValues) {
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Destination.entries.forEach(::composable)
        }
    }

    if (compactWidth || compactHeight) {
        val savedDestinations =
            rememberSaveable(saver = destinationMapSaver()) { mutableStateMapOf() }
        Scaffold(
            topBar = {
                val entry by navController.currentBackStackEntryAsState()
                val destination = entry?.asDestination()
                val destinations = when {
                    destination == null -> emptyList()
                    destination.replaced.isNotEmpty() -> listOf(destination) + destination.replaced
                    destination.replacement != null -> listOf(destination.replacement) + destination.replacement.replaced
                    else -> emptyList()
                }

                TopBar(
                    navController = navController,
                    destinations = destinations,
                    onClickDestination = {
                        if (it.replacement != null) {
                            savedDestinations[it.replacement] = it
                        }

                        navController.sail(it)
                    }
                )
            },
            bottomBar = {
                BottomNavigation(
                    navController = navController,
                    destinations = Destination.essentials,
                    onClickDestination = {
                        navController.sail(savedDestinations.getOrDefault(it, it))
                    }
                )
            }
        ) {
            Host(it)
        }
    } else {
        Scaffold {
            SideNavigation(
                navController = navController,
                destinations = Destination.entries,
                windowPadding = it,
                onClickDestination = navController::navigate,
            ) { contentPadding ->
                Column {
                    TopBar(navController, onClickDestination = navController::navigate)
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
            it.forEach { (key, value) -> put(Destination.byRoute(key)!!, value as Destination) }
        }
    }
)
