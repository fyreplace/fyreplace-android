package app.fyreplace.fyreplace.legacy.ui

import android.view.View
import androidx.annotation.StringRes
import app.fyreplace.fyreplace.legacy.extensions.HapticType
import app.fyreplace.fyreplace.legacy.extensions.provideHapticFeedback
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

interface BasePresenter : ContextHolder {
    val rootView: View?

    fun showBasicSnackbar(
        @StringRes message: Int,
        @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG,
        haptics: HapticType? = null
    ) {
        val rootView = rootView ?: return
        Snackbar.make(rootView, rootView.context.getString(message), duration).show()

        if (haptics != null) {
            rootView.provideHapticFeedback(haptics)
        }
    }
}
