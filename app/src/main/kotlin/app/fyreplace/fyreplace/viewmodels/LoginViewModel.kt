package app.fyreplace.fyreplace.viewmodels

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.IntegerRes
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.storeAuthToken
import app.fyreplace.fyreplace.grpc.defaultClient
import app.fyreplace.protos.AccountServiceGrpcKt
import app.fyreplace.protos.connectionCredentials
import app.fyreplace.protos.email
import app.fyreplace.protos.userCreation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class LoginViewModel(
    private val resources: Resources,
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub
) : LoadingViewModel() {
    private val mIsRegistering = MutableStateFlow(false)
    val isRegistering = mIsRegistering.asStateFlow()
    val email = MutableStateFlow("")
    val username = MutableStateFlow("")
    val canProceed = isRegistering
        .combine(username) { registering, username ->
            !registering || username.between(
                R.integer.username_min_size,
                R.integer.username_max_size
            )
        }
        .combine(email) { res, email ->
            res && email.between(
                R.integer.email_min_size,
                R.integer.email_max_size
            )
        }
        .asState(false)

    fun setIsRegistering(registering: Boolean) {
        mIsRegistering.value = registering
    }

    suspend fun register(): Unit = whileLoading {
        accountStub.create(userCreation {
            email = this@LoginViewModel.email.value
            username = this@LoginViewModel.username.value
        })
    }

    suspend fun login(): Unit = whileLoading {
        accountStub.sendConnectionEmail(email {
            email = this@LoginViewModel.email.value
        })
    }

    suspend fun login(password: String): Unit = whileLoading {
        val token = accountStub.connect(connectionCredentials {
            email = this@LoginViewModel.email.value
            this.password = password
            client = defaultClient
        })
        preferences.storeAuthToken(token)
    }

    private fun String.between(@IntegerRes a: Int, @IntegerRes b: Int) =
        isNotBlank() && length in resources.getInteger(a)..resources.getInteger(b)
}
