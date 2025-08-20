package app.fyreplace.fyreplace.legacy

import app.fyreplace.fyreplace.BaseApp
import org.conscrypt.Conscrypt
import java.security.Security

abstract class SecureApp : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
