package app.fyreplace.fyreplace.legacy.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

val View.insets
    get() = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)

fun View.updateBottomPaddingWithSystemInset(basePadding: Insets) =
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            top = basePadding.top + systemBars.top,
            left = basePadding.left + systemBars.left,
            right = basePadding.right + systemBars.right,
            bottom = basePadding.bottom + systemBars.bottom
        )
        return@setOnApplyWindowInsetsListener insets
    }

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
