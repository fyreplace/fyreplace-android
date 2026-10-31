package app.fyreplace.fyreplace.legacy.extensions

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import app.fyreplace.fyreplace.R

const val PREF_AUTH_TOKEN = "auth.token"
const val PREF_SETTINGS_THEME = "settings.theme"
const val PREF_APP_ENVIRONMENT = "app.environment"

fun SharedPreferences.applyTheme(context: Context) {
    val themeValue = getString(
        PREF_SETTINGS_THEME,
        context.getString(R.string.settings_theme_auto_value)
    )
    val nightMode = when (themeValue) {
        context.getString(R.string.settings_theme_light_value) -> AppCompatDelegate.MODE_NIGHT_NO
        context.getString(R.string.settings_theme_dark_value) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }
    AppCompatDelegate.setDefaultNightMode(nightMode)
}
