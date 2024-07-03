package app.fyreplace.fyreplace.ui.views.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.screens.ArchiveScreen
import app.fyreplace.fyreplace.ui.screens.DraftsScreen
import app.fyreplace.fyreplace.ui.screens.FeedScreen
import app.fyreplace.fyreplace.ui.screens.NotificationsScreen
import app.fyreplace.fyreplace.ui.screens.PublishedScreen
import app.fyreplace.fyreplace.ui.screens.SettingsScreen

@Immutable
enum class Destination(
    val route: String,
    val replacement: Destination? = null,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    @StringRes val labelRes: Int,
    val content: @Composable () -> Unit
) {
    FEED(
        route = "feed",
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
        labelRes = R.string.main_destination_feed,
        content = { FeedScreen() }
    ),
    NOTIFICATIONS(
        route = "notifications",
        activeIcon = Icons.Filled.Notifications,
        inactiveIcon = Icons.Outlined.Notifications,
        labelRes = R.string.main_destination_notifications,
        content = { NotificationsScreen() }
    ),
    ARCHIVE(
        route = "archive",
        replacement = NOTIFICATIONS,
        activeIcon = Icons.Filled.BookmarkAdded,
        inactiveIcon = Icons.Outlined.BookmarkAdded,
        labelRes = R.string.main_destination_archive,
        content = { ArchiveScreen() }
    ),
    DRAFTS(
        route = "drafts",
        activeIcon = Icons.Filled.Description,
        inactiveIcon = Icons.Outlined.Description,
        labelRes = R.string.main_destination_drafts,
        content = { DraftsScreen() }
    ),
    PUBLISHED(
        route = "published",
        replacement = DRAFTS,
        activeIcon = Icons.Filled.Inventory2,
        inactiveIcon = Icons.Outlined.Inventory2,
        labelRes = R.string.main_destination_published,
        content = { PublishedScreen() }
    ),
    SETTINGS(
        route = "settings",
        activeIcon = Icons.Filled.AccountCircle,
        inactiveIcon = Icons.Outlined.AccountCircle,
        labelRes = R.string.main_destination_settings,
        content = { SettingsScreen() }
    );

    val replaced by lazy { Destination.entries.filter { it.replacement == this } }

    override fun toString() = route

    companion object {
        val essentials = entries.filter { it.replacement == null }

        fun byRoute(route: String) = entries.find { it.route == route }
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

fun NavBackStackEntry.asDestination() = Destination.entries.find { it.route == destination.route }

fun NavBackStackEntry?.isAt(destination: Destination, exactly: Boolean = true): Boolean = when {
    this?.destination?.route == destination.route -> true
    exactly -> false
    this?.asDestination()?.replacement == destination -> true
    else -> false
}

fun NavGraphBuilder.composable(destination: Destination) =
    composable(destination.route) { destination.content() }

fun NavController.sail(destination: Destination) = navigate(destination.route) {
    popUpTo(graph.startDestinationId) {
        saveState = true
    }

    launchSingleTop = true
    restoreState = true
}
