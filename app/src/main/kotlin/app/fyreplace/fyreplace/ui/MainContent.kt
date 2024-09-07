package app.fyreplace.fyreplace.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import app.fyreplace.fyreplace.input.DestinationKeyboardShortcut
import app.fyreplace.fyreplace.input.getShortcut
import app.fyreplace.fyreplace.ui.screens.ArchiveScreen
import app.fyreplace.fyreplace.ui.screens.DraftsScreen
import app.fyreplace.fyreplace.ui.screens.FeedScreen
import app.fyreplace.fyreplace.ui.screens.LoginScreen
import app.fyreplace.fyreplace.ui.screens.NotificationsScreen
import app.fyreplace.fyreplace.ui.screens.PublishedScreen
import app.fyreplace.fyreplace.ui.screens.RegisterScreen
import app.fyreplace.fyreplace.ui.screens.SettingsScreen
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.bars.TopBar
import app.fyreplace.fyreplace.ui.views.navigation.BottomNavigation
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.SideNavigation
import app.fyreplace.fyreplace.ui.views.navigation.navigatePoppingBackStack
import app.fyreplace.fyreplace.ui.views.navigation.toSingletonDestination
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MainContent() {
    val viewModel = hiltViewModel<MainViewModel>()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val failure by viewModel.currentFailure.collectAsStateWithLifecycle()

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val compactWidth = sizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val compactHeight = sizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    val compact = compactWidth || compactHeight

    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry?.toSingletonDestination()
    val destinationGroups by remember {
        derivedStateOf {
            topLevelDestinationGroups(expanded = !compact, userAuthenticated = isAuthenticated)
        }
    }
    val currentDestinationGroup =
        destinationGroups.find { (it.choices + it.root).contains(currentDestination) }
    val savedDestinations = rememberSaveable(saver = destinationMapSaver()) { mutableStateMapOf() }

    fun onClickDestination(destination: Destination.Singleton) {
        val actualDestination = destinationGroups.find { it.root == destination }
            ?.choices
            ?.firstOrNull()
            ?: destination
        navController.navigatePoppingBackStack(
            savedDestinations[actualDestination] ?: actualDestination
        )
    }

    val keyboardHandler = Modifier.onKeyEvent { event ->
        when (event.type) {
            KeyEventType.KeyUp -> when (val shortcut = getShortcut(event)) {
                is DestinationKeyboardShortcut -> {
                    if (!shortcut.destination.requiresAuthentication || isAuthenticated) {
                        onClickDestination(shortcut.destination)
                    }

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
            startDestination = Destination.Feed,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable<Destination.Feed> { FeedScreen() }
            composable<Destination.Notifications> { NotificationsScreen() }
            composable<Destination.Archive> { ArchiveScreen() }
            composable<Destination.Drafts> { DraftsScreen() }
            composable<Destination.Published> { PublishedScreen() }
            composable<Destination.Settings> { SettingsScreen() }
            composable<Destination.Login> { LoginScreen(visibilityScope = this) }
            composable<Destination.Register> { RegisterScreen(visibilityScope = this) }
        }
    }

    @Composable
    fun Top() {
        TopBar(
            destinations = currentDestinationGroup?.choices ?: emptyList(),
            selectedDestination = currentDestination,
            onClickDestination = {
                navController.navigatePoppingBackStack(it)

                if (currentDestinationGroup?.defaultDestination != null) {
                    savedDestinations[currentDestinationGroup.defaultDestination] = it
                }
            }
        )
    }

    if (compact) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Top()
            },
            bottomBar = {
                BottomNavigation(
                    destinations = destinationGroups.map(Destination.Singleton.Group::root),
                    selectedDestination = currentDestinationGroup?.root,
                    onClickDestination = ::onClickDestination
                )
            }
        ) {
            Host(it, modifier = keyboardHandler)
        }
    } else {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) {
            SideNavigation(
                destinations = destinationGroups.map(Destination.Singleton.Group::root),
                selectedDestination = currentDestinationGroup?.root,
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

    val smartCastableFailure = failure

    if (smartCastableFailure != null) {
        FailureDialog(failure = smartCastableFailure, dismiss = viewModel::dismissError)
    }

    LaunchedEffect(isAuthenticated) {
        val accountEntryDestinations = setOf(Destination.Login, Destination.Register)
        val newDestination = when {
            isAuthenticated && currentDestination in accountEntryDestinations -> Destination.Settings
            !isAuthenticated && currentDestination == Destination.Settings -> Destination.Login
            !isAuthenticated && currentDestination?.requiresAuthentication ?: false -> Destination.Feed
            else -> null
        }

        newDestination?.let(navController::navigatePoppingBackStack)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(scope) {
        scope.launch {
            viewModel.snackbarEvents.collect {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(it.message),
                    actionLabel = it.action?.label?.let(context::getString),
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )

                if (result == SnackbarResult.ActionPerformed) {
                    it.action?.action?.invoke(context)
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

private fun topLevelDestinationGroups(expanded: Boolean, userAuthenticated: Boolean) =
    Destination.Singleton.all
        .filter { it.parent == null || (expanded && !it.parent!!.keepsChildren) }
        .map { destination ->
            Destination.Singleton.Group(
                root = destination,
                choices = Destination.Singleton.all.filter { it.parent == destination }
                    .let { mutableListOf(destination).apply { addAll(it) } }
                    .filter {
                        when {
                            destination != Destination.Settings -> true
                            else -> (it == Destination.Settings) == userAuthenticated
                        }
                    }
                    .takeIf { it.size > 1 && (!expanded || destination.keepsChildren) }
                    ?: emptyList()
            )
        }

private fun destinationMapSaver() = mapSaver(
    save = {
        mutableMapOf<String, Destination.Singleton>().apply {
            it.forEach { (key, value) -> put(key::class.qualifiedName!!, value) }
        }
    },
    restore = {
        mutableMapOf<Destination.Singleton, Destination.Singleton>().apply {
            it.forEach { (key, value) ->
                put(
                    requireNotNull(Class.forName(key).kotlin.objectInstance) as Destination.Singleton,
                    value as Destination.Singleton
                )
            }
        }
    }
)
