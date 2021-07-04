package app.fyreplace.client

import android.app.Application
import com.google.android.gms.security.ProviderInstaller

abstract class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ProviderInstaller.installIfNeeded(this)
    }
}
