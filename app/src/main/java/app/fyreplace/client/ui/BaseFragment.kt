package app.fyreplace.client.ui

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.fyreplace.client.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    protected fun showBasicAlert(
        @StringRes title: Int,
        @StringRes message: Int,
        error: Boolean = false
    ) {
        MaterialAlertDialogBuilder(context ?: return)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .run { if (error) setIcon(R.drawable.ic_baseline_error) else this }
            .show()
    }
}
