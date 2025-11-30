package app.fyreplace.fyreplace.legacy

import android.content.Intent
import app.fyreplace.fyreplace.BaseApp
import com.google.android.gms.security.ProviderInstaller

abstract class SecureApp : BaseApp(), ProviderInstaller.ProviderInstallListener {
    override fun onCreate() {
        super.onCreate()
        ProviderInstaller.installIfNeededAsync(this, this)
    }

    override fun onProviderInstalled() = Unit

    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) = Unit
}
