package app.fyreplace.fyreplace.ui

import android.view.View
import androidx.annotation.StringRes
import app.fyreplace.fyreplace.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

interface BasePresenter {
    val rootView: View?

    fun showBasicAlert(@StringRes title: Int, @StringRes message: Int?, error: Boolean = false) {
        MaterialAlertDialogBuilder(rootView?.context ?: return)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } ?: setMessage(null) }
            .setPositiveButton(R.string.ok, null)
            .run { if (error) setIcon(R.drawable.ic_baseline_error) else this }
            .show()
    }

    fun showChoiceAlert(@StringRes title: Int, @StringRes message: Int?, action: () -> Unit) {
        MaterialAlertDialogBuilder(rootView?.context ?: return)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } ?: setMessage(null) }
            .setPositiveButton(R.string.yes) { _, _ -> action() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    fun showBasicSnackbar(@StringRes message: Int) {
        val rootView = rootView ?: return
        Snackbar.make(rootView, rootView.context.getString(message), Snackbar.LENGTH_LONG).show()
    }
}
