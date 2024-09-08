package app.fyreplace.fyreplace

import android.app.AlertDialog
import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller

abstract class SecureActivity : ComponentActivity(), ProviderInstaller.ProviderInstallListener {
    override fun onPostResume() {
        super.onPostResume()
        ProviderInstaller.installIfNeededAsync(this, this)
    }

    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        with(GoogleApiAvailability.getInstance()) {
            if (isUserResolvableError(errorCode)) {
                showErrorDialogFragment(this@SecureActivity, errorCode, ERROR_DIALOG_REQUEST_CODE) {
                    warnUser()
                }
            } else {
                warnUser()
            }
        }
    }

    override fun onProviderInstalled() = Unit

    private fun warnUser() {
        AlertDialog.Builder(this)
            .setTitle(R.string.secure_warning_title)
            .setMessage(R.string.secure_warning_description)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    companion object {
        const val ERROR_DIALOG_REQUEST_CODE = 1
    }
}
