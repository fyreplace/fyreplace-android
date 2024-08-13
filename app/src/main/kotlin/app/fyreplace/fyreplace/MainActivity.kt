package app.fyreplace.fyreplace

import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import androidx.activity.SystemBarStyle
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.fyreplace.fyreplace.input.keyboardShortcuts
import app.fyreplace.fyreplace.ui.MainContent
import app.fyreplace.fyreplace.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

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
