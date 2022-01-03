package app.fyreplace.client.ui

import android.view.View
import androidx.annotation.StringRes
import app.fyreplace.client.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

interface BasePresenter {
    val rootView: View?

    fun showBasicAlert(@StringRes title: Int, @StringRes message: Int, error: Boolean = false) {
        MaterialAlertDialogBuilder(rootView?.context ?: return)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .run { if (error) setIcon(R.drawable.ic_baseline_error) else this }
            .show()
    }

    fun showBasicSnackbar(@StringRes message: Int) {
        val rootView = rootView ?: return
        Snackbar.make(rootView, rootView.context.getString(message), Snackbar.LENGTH_LONG).show()
    }
}
