package app.fyreplace.client.ui

import android.content.Context
import androidx.annotation.StringRes
import app.fyreplace.client.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

interface BasePresenter {
    fun getContext(): Context?

    fun showBasicAlert(
        @StringRes title: Int,
        @StringRes message: Int,
        error: Boolean = false
    ) {
        MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .run { if (error) setIcon(R.drawable.ic_baseline_error) else this }
            .show()
    }
}
