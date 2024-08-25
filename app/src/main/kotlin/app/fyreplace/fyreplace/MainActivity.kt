package app.fyreplace.fyreplace

import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import androidx.activity.SystemBarStyle
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.fyreplace.fyreplace.api.TokenRefreshWorker
import app.fyreplace.fyreplace.input.keyboardShortcuts
import app.fyreplace.fyreplace.ui.MainContent
import app.fyreplace.fyreplace.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

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

            ReportDrawn()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TokenRefreshWorker::class.java.name,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<TokenRefreshWorker>(1.days.toJavaDuration())
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
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
