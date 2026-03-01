package app.fyreplace.fyreplace.legacy.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.StringRes
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

    fun View.provideHapticFeedback(type: HapticType = HapticType.SIMPLE) {
        performHapticFeedback(
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> when (type) {
                    HapticType.TOGGLE_ON -> HapticFeedbackConstants.TOGGLE_ON
                    HapticType.TOGGLE_OFF -> HapticFeedbackConstants.TOGGLE_OFF
                    else -> HapticFeedbackConstants.CONFIRM
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                else -> HapticFeedbackConstants.VIRTUAL_KEY
            }
        )
    }

    fun View.provideHapticFeedback(positive: Boolean) = provideHapticFeedback(
        if (positive) HapticType.TOGGLE_ON
        else HapticType.TOGGLE_OFF
    )

    enum class HapticType {
        TOGGLE_ON,
        TOGGLE_OFF,
        SIMPLE
    }
}
