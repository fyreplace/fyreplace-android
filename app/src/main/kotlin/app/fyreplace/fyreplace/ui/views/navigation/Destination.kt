package app.fyreplace.fyreplace.ui.views.navigation

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import app.fyreplace.fyreplace.R
import kotlinx.serialization.Serializable

sealed interface Destination {
    sealed interface Singleton : Destination {
        val parent: Singleton?
        val keepsChildren: Boolean
        val activeIcon: ImageVector
        val inactiveIcon: ImageVector

        @get:StringRes
        val labelRes: Int

        companion object {
            val all = Destination::class.nestedClasses
                .filter { it.java.interfaces.contains(Singleton::class.java) }
                .map { it.objectInstance as Singleton }
        }

        data class Group(
            val root: Singleton,
            val choices: List<Singleton> = emptyList()
        ) {
            val defaultDestination = choices.firstOrNull() ?: root
        }
    }

    @Serializable
    data object Feed : Singleton {
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Home
        override val inactiveIcon = Icons.Outlined.Home
        override val labelRes = R.string.main_destination_feed
    }

    @Serializable
    data object Notifications : Singleton {
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Notifications
        override val inactiveIcon = Icons.Outlined.Notifications
        override val labelRes = R.string.main_destination_notifications
    }

    @Serializable
    data object Archive : Singleton {
        override val parent = Notifications
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.BookmarkAdded
        override val inactiveIcon = Icons.Outlined.BookmarkAdded
        override val labelRes = R.string.main_destination_archive
    }

    @Serializable
    data object Drafts : Singleton {
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Description
        override val inactiveIcon = Icons.Outlined.Description
        override val labelRes = R.string.main_destination_drafts
    }

    @Serializable
    data object Published : Singleton {
        override val parent = Drafts
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Inventory2
        override val inactiveIcon = Icons.Outlined.Inventory2
        override val labelRes = R.string.main_destination_published
    }

    @Serializable
    data object Settings : Singleton {
        override val parent = null
        override val keepsChildren = true
        override val activeIcon = Icons.Filled.Settings
        override val inactiveIcon = Icons.Outlined.Settings
        override val labelRes = R.string.main_destination_settings
    }

    @Serializable
    data object Login : Singleton {
        override val parent = Settings
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.AccountCircle
        override val inactiveIcon = Icons.Outlined.AccountCircle
        override val labelRes = R.string.main_destination_login
    }

    @Serializable
    data object Register : Singleton {
        override val parent = Settings
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.AddCircle
        override val inactiveIcon = Icons.Outlined.AddCircleOutline
        override val labelRes = R.string.main_destination_register
    }
}

@Composable
fun Icon(destination: Destination.Singleton, active: Boolean) {
    Icon(
        if (active) destination.activeIcon else destination.inactiveIcon,
        contentDescription = stringResource(destination.labelRes)
    )
}

@Composable
fun Text(destination: Destination.Singleton) {
    val crampedText = booleanResource(R.bool.cramped_width)
    Text(
        stringResource(destination.labelRes),
        fontSize = if (crampedText) 10.sp else TextUnit.Unspecified,
        letterSpacing = if (crampedText) 0.sp else TextUnit.Unspecified
    )
}

fun NavBackStackEntry.toSingletonDestination() =
    Destination.Singleton.all.first { destinationClass ->
        destination.hierarchy.any {
            it.route == destinationClass::class.qualifiedName
        }
    }

fun NavController.navigatePoppingBackStack(destination: Destination) = navigate(destination) {
    popUpTo(graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
