package app.fyreplace.fyreplace.legacy

import app.fyreplace.fyreplace.BaseApp
import com.google.android.gms.security.ProviderInstaller

abstract class SecureApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        ProviderInstaller.installIfNeeded(this)
    }
}
