package app.fyreplace.fyreplace

import android.os.Bundle
import androidx.activity.ComponentActivity
import org.conscrypt.Conscrypt
import java.security.Security

abstract class SecureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
