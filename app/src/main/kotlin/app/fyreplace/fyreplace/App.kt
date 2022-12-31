package app.fyreplace.fyreplace

import android.content.SharedPreferences
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

        scope.launch {
            em.events.filterIsInstance<ActivityWasStartedEvent>().collect { activityCount++ }
        }

        scope.launch {
            em.events.filterIsInstance<ActivityWasStoppedEvent>().collect { activityCount-- }
        }
    }
}
