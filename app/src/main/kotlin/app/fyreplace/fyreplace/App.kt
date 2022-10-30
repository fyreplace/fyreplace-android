package app.fyreplace.fyreplace

import android.content.SharedPreferences
import app.fyreplace.fyreplace.extensions.applySettings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp() {
    @Inject
    lateinit var preferences: SharedPreferences

    private var activityCount = 0
    val isInForeground get() = activityCount > 0

    override fun onCreate() {
        super.onCreate()
        preferences.applySettings(this)
    }

    fun registerActivityStart() {
        activityCount++
    }

    fun registerActivityStop() {
        activityCount--
    }
}
