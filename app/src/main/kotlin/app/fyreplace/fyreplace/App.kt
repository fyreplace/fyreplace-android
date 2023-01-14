package app.fyreplace.fyreplace

import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import app.fyreplace.fyreplace.broadcasts.NetworkBroadcastReceiver
import app.fyreplace.fyreplace.events.ActivityWasStartedEvent
import app.fyreplace.fyreplace.events.ActivityWasStoppedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.extensions.applySettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp() {
    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var em: EventsManager

    private val scope = MainScope()
    private var activityCount = 0
    val isInForeground get() = activityCount > 0

    override fun onCreate() {
        super.onCreate()
        preferences.applySettings(this)
        ContextCompat.registerReceiver(
            this,
            NetworkBroadcastReceiver(),
            IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )

        scope.launch {
            em.events.filterIsInstance<ActivityWasStartedEvent>().collect { activityCount++ }
        }

        scope.launch {
            em.events.filterIsInstance<ActivityWasStoppedEvent>().collect { activityCount-- }
        }
    }
}
