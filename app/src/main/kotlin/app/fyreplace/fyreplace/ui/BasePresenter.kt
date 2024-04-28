package app.fyreplace.fyreplace.ui

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

interface BasePresenter : ContextHolder {
    val rootView: View?

    fun showBasicSnackbar(@StringRes message: Int) {
        val rootView = rootView ?: return
        Snackbar.make(rootView, rootView.context.getString(message), Snackbar.LENGTH_LONG).show()
    }
}
