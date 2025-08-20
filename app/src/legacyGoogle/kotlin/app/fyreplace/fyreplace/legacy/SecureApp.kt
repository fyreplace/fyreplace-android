package app.fyreplace.fyreplace.legacy

import app.fyreplace.fyreplace.BaseApp
import com.google.android.gms.security.ProviderInstaller
import org.conscrypt.Conscrypt
import java.security.Security

abstract class SecureApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (_: Exception) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }
    }
}
