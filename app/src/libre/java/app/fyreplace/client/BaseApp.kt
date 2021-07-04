package app.fyreplace.client

import android.app.Application
import org.conscrypt.Conscrypt
import java.security.Security

abstract class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
