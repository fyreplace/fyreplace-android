package app.fyreplace.fyreplace.extensions

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

fun View.hideSoftInput() {
    ContextCompat.getSystemService(context, InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(windowToken, 0)
}
