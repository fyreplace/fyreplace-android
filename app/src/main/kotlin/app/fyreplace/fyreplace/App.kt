package app.fyreplace.fyreplace

import android.content.SharedPreferences
import app.fyreplace.fyreplace.extensions.applySettings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp() {
    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences.applySettings(this)
    }
}
