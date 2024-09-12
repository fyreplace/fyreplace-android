package app.fyreplace.fyreplace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import androidx.activity.SystemBarStyle
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.fyreplace.fyreplace.api.TokenRefreshWorker
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.input.keyboardShortcuts
import app.fyreplace.fyreplace.ui.MainContent
import app.fyreplace.fyreplace.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@AndroidEntryPoint
class MainActivity : SecureActivity() {
    @Inject
    lateinit var eventBus: EventBus

    private var lastUriHandled: Uri? = null

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
        lastUriHandled = savedInstanceState?.getString(::lastUriHandled.name)?.let(Uri::parse)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(::lastUriHandled.name, lastUriHandled?.toString())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
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

    private fun handleIntent(intent: Intent) {
        if (intent.data == lastUriHandled) {
            return
        }

        val uri = intent.data
        lastUriHandled = uri

        if (uri != null) lifecycleScope.launch {
            eventBus.publish(Event.DeepLink(uri))
        }
    }
}
