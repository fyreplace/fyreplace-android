package app.fyreplace.fyreplace.legacy.extensions

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

val View.insets
    get() = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)

fun View.updatePaddingWithSystemInsets(basePadding: Insets) =
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.updatePadding(basePadding + insets.getInsets(WindowInsetsCompat.Type.systemBars()))
        return@setOnApplyWindowInsetsListener insets
    }

fun View.updatePaddingWithImeInsets(basePadding: Insets) =
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.updatePadding(basePadding + insets.getInsets(WindowInsetsCompat.Type.ime()))
        return@setOnApplyWindowInsetsListener insets
    }

fun View.updatePadding(insets: Insets) =
    updatePadding(insets.left, insets.top, insets.right, insets.bottom)

fun View.showSoftInput() {
    if (requestFocus()) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideSoftInput() {
    context.getSystemService(InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(windowToken, 0)
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
