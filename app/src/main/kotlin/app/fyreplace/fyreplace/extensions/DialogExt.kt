package app.fyreplace.fyreplace.extensions

import android.app.Dialog
import android.view.WindowManager

fun Dialog.showSoftInput() {
    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}
