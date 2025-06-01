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

@Serializable
sealed interface Destination {
    @get:StringRes
    val labelRes: Int
    val hasMainAction get() = false

    companion object {
        val all = listOf(
            Feed,
            Notifications,
            Archive,
            Drafts,
            Published,
            Settings,
            Emails,
            Login(),
            Register()
        )
    }

    sealed interface Singleton : Destination {
        val parent: Singleton?
        val keepsChildren: Boolean
        val activeIcon: ImageVector
        val inactiveIcon: ImageVector
        val requiresAuthentication: Boolean

        companion object {
            val all = Destination.all.filterIsInstance<Singleton>()
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
        override val labelRes = R.string.main_destination_feed
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Home
        override val inactiveIcon = Icons.Outlined.Home
        override val requiresAuthentication = false
    }

    @Serializable
    data object Notifications : Singleton {
        override val labelRes = R.string.main_destination_notifications
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Notifications
        override val inactiveIcon = Icons.Outlined.Notifications
        override val requiresAuthentication = true
    }

    @Serializable
    data object Archive : Singleton {
        override val labelRes = R.string.main_destination_archive
        override val parent = Notifications
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.BookmarkAdded
        override val inactiveIcon = Icons.Outlined.BookmarkAdded
        override val requiresAuthentication = true
    }

    @Serializable
    data object Drafts : Singleton {
        override val labelRes = R.string.main_destination_drafts
        override val parent = null
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Description
        override val inactiveIcon = Icons.Outlined.Description
        override val requiresAuthentication = true
    }

    @Serializable
    data object Published : Singleton {
        override val labelRes = R.string.main_destination_published
        override val parent = Drafts
        override val keepsChildren = false
        override val activeIcon = Icons.Filled.Inventory2
        override val inactiveIcon = Icons.Outlined.Inventory2
        override val requiresAuthentication = true
    }

    @Serializable
    data object Settings : Singleton {
        override val labelRes = R.string.main_destination_settings
        override val parent = null
        override val keepsChildren = true
        override val activeIcon = Icons.Filled.Settings
        override val inactiveIcon = Icons.Outlined.Settings
        override val requiresAuthentication = false
    }

    @Serializable
    data class Login(val randomCode: String? = null) : Singleton {
        override val labelRes = R.string.main_destination_login
        override val parent get() = Settings
        override val keepsChildren = false
        override val activeIcon get() = Icons.Filled.AccountCircle
        override val inactiveIcon get() = Icons.Outlined.AccountCircle
        override val requiresAuthentication = false
    }

    @Serializable
    data class Register(val randomCode: String? = null) : Singleton {
        override val labelRes = R.string.main_destination_register
        override val parent get() = Settings
        override val keepsChildren = false
        override val activeIcon get() = Icons.Filled.AddCircle
        override val inactiveIcon get() = Icons.Outlined.AddCircleOutline
        override val requiresAuthentication = false
    }

    @Serializable
    data object Emails : Destination {
        override val labelRes = R.string.main_destination_emails
        override val hasMainAction get() = true
    }
}

@Composable
fun Icon(destination: Destination.Singleton, active: Boolean) {
    Icon(
        imageVector = if (active) destination.activeIcon else destination.inactiveIcon,
        contentDescription = stringResource(destination.labelRes)
    )
}

@Composable
fun Text(destination: Destination.Singleton) {
    val crampedText = booleanResource(R.bool.cramped_width)
    Text(
        text = stringResource(destination.labelRes),
        fontSize = if (crampedText) 10.sp else TextUnit.Unspecified,
        letterSpacing = if (crampedText) 0.sp else TextUnit.Unspecified
    )
}

fun NavBackStackEntry.toDestination() = destination.hierarchy.firstNotNullOfOrNull {
    Destination.all.firstOrNull { destinationClass ->
        val routeName = destinationClass::class.qualifiedName ?: return@firstOrNull false
        return@firstOrNull it.route?.startsWith(routeName) == true
    }
}

fun NavController.navigatePoppingBackStack(destination: Destination) = navigate(destination) {
    if (destination is Destination.Singleton) {
        popUpTo(graph.startDestinationId)
        launchSingleTop = true
    }
}
