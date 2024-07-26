package app.fyreplace.fyreplace.input

import android.content.Context
import android.view.KeyEvent
import android.view.KeyboardShortcutInfo
import androidx.annotation.StringRes
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import app.fyreplace.fyreplace.ui.views.navigation.Destination

abstract class KeyboardShortcut(
    @StringRes val labelRes: Int,
    val key: Key,
    val ctrl: Boolean,
    val alt: Boolean
) {
    fun getInfo(context: Context) = KeyboardShortcutInfo(
        context.getString(labelRes),
        key.nativeKeyCode,
        (if (ctrl) KeyEvent.META_CTRL_ON else 0) or (if (alt) KeyEvent.META_ALT_ON else 0)
    )
}

class DestinationKeyboardShortcut(val destination: Destination, key: Key) :
    KeyboardShortcut(destination.labelRes, key, false, true)

val keyboardShortcuts: List<KeyboardShortcut> = listOf(
    DestinationKeyboardShortcut(Destination.FEED, Key.One),
    DestinationKeyboardShortcut(Destination.NOTIFICATIONS, Key.Two),
    DestinationKeyboardShortcut(Destination.ARCHIVE, Key.Three),
    DestinationKeyboardShortcut(Destination.DRAFTS, Key.Four),
    DestinationKeyboardShortcut(Destination.PUBLISHED, Key.Five),
    DestinationKeyboardShortcut(Destination.SETTINGS, Key.Six)
)

fun getShortcut(keyEvent: androidx.compose.ui.input.key.KeyEvent) = keyboardShortcuts.find {
    it.key == keyEvent.key && it.ctrl == keyEvent.isCtrlPressed && it.alt == keyEvent.isAltPressed
}
