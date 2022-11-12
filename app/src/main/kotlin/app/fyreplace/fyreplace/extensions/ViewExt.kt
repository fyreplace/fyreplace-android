package app.fyreplace.fyreplace.extensions

import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideSoftInput() {
    context.getSystemService(InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(windowToken, 0)
}
