package app.fyreplace.fyreplace.ui.views.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.screens.ArchiveScreen
import app.fyreplace.fyreplace.ui.screens.DraftsScreen
import app.fyreplace.fyreplace.ui.screens.FeedScreen
import app.fyreplace.fyreplace.ui.screens.LoginScreen
import app.fyreplace.fyreplace.ui.screens.NotificationsScreen
import app.fyreplace.fyreplace.ui.screens.PublishedScreen
import app.fyreplace.fyreplace.ui.screens.RegisterScreen
import app.fyreplace.fyreplace.ui.screens.SettingsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Immutable
enum class Destination(
    val route: String,
    val parent: Destination? = null,
    val keepsChildren: Boolean = false,
    val visible: () -> Boolean = { true },
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    @StringRes val labelRes: Int,
    val content: @Composable (SharedTransitionScope, AnimatedVisibilityScope) -> Unit
) {
    FEED(
        route = "feed",
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
        labelRes = R.string.main_destination_feed,
        content = { _, _ -> FeedScreen() }
    ),
    NOTIFICATIONS(
        route = "notifications",
        activeIcon = Icons.Filled.Notifications,
        inactiveIcon = Icons.Outlined.Notifications,
        labelRes = R.string.main_destination_notifications,
        content = { _, _ -> NotificationsScreen() }
    ),
    ARCHIVE(
        route = "archive",
        parent = NOTIFICATIONS,
        activeIcon = Icons.Filled.BookmarkAdded,
        inactiveIcon = Icons.Outlined.BookmarkAdded,
        labelRes = R.string.main_destination_archive,
        content = { _, _ -> ArchiveScreen() }
    ),
    DRAFTS(
        route = "drafts",
        activeIcon = Icons.Filled.Description,
        inactiveIcon = Icons.Outlined.Description,
        labelRes = R.string.main_destination_drafts,
        content = { _, _ -> DraftsScreen() }
    ),
    PUBLISHED(
        route = "published",
        parent = DRAFTS,
        activeIcon = Icons.Filled.Inventory2,
        inactiveIcon = Icons.Outlined.Inventory2,
        labelRes = R.string.main_destination_published,
        content = { _, _ -> PublishedScreen() }
    ),
    SETTINGS(
        route = "settings",
        keepsChildren = true,
        visible = { false },
        activeIcon = Icons.Filled.Settings,
        inactiveIcon = Icons.Outlined.Settings,
        labelRes = R.string.main_destination_settings,
        content = { _, _ -> SettingsScreen() }
    ),
    LOGIN(
        route = "login",
        parent = SETTINGS,
        activeIcon = Icons.Filled.AccountCircle,
        inactiveIcon = Icons.Outlined.AccountCircle,
        labelRes = R.string.main_destination_login,
        content = { t, v -> t.LoginScreen(v) }
    ),
    REGISTER(
        route = "register",
        parent = SETTINGS,
        activeIcon = Icons.Filled.AddCircle,
        inactiveIcon = Icons.Outlined.AddCircleOutline,
        labelRes = R.string.main_destination_register,
        content = { t, v -> t.RegisterScreen(v) }
    );

    override fun toString() = route

    companion object {
        fun byRoute(route: String) = entries.find { it.route == route }
    }

    @Immutable
    data class Set(val root: Destination, val choices: List<Destination> = emptyList()) {
        val defaultDestination = choices.firstOrNull() ?: root

        companion object {
            fun topLevel(flatten: Boolean) = entries.mapNotNull { destination ->
                if (destination.parent != null && (!flatten || destination.parent.keepsChildren)) null
                else Set(
                    root = destination,
                    choices = entries.filter { it.parent == destination }
                        .let { mutableListOf(destination).apply { addAll(it) } }
                        .filter { it.visible() }
                        .takeIf { it.size > 1 && (!flatten || destination.keepsChildren) }
                        ?: emptyList()
                )
            }
        }
    }
}

@Composable
fun Icon(destination: Destination, selected: Boolean) {
    Icon(
        if (selected) destination.activeIcon else destination.inactiveIcon,
        contentDescription = stringResource(destination.labelRes)
    )
}

@Composable
fun Text(destination: Destination) {
    val crampedText = booleanResource(R.bool.cramped_width)
    Text(
        stringResource(destination.labelRes),
        fontSize = if (crampedText) 10.sp else TextUnit.Unspecified,
        letterSpacing = if (crampedText) 0.sp else TextUnit.Unspecified
    )
}

fun NavBackStackEntry?.asDestination() = Destination.byRoute(this?.destination?.route ?: "")

fun NavController.sail(destination: Destination) = navigate(destination.route) {
    popUpTo(graph.startDestinationId) {
        saveState = true
    }

    launchSingleTop = true
    restoreState = true
}
