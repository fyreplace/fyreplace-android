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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LoginViewModel(
    private val resources: Resources,
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub
) : BaseViewModel() {
    private val mIsRegistering = MutableStateFlow(false)
    private val mIsLoading = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = mIsRegistering
    val email = MutableStateFlow("")
    val username = MutableStateFlow("")
    val isLoading: StateFlow<Boolean> = mIsLoading
    val canProceed = isRegistering
        .combine(username) { registering, username ->
            !registering || username.between(
                R.integer.login_username_min_size,
                R.integer.login_username_max_size
            )
        }
        .combine(email) { res, email ->
            res && email.between(
                R.integer.login_email_min_size,
                R.integer.login_email_max_size
            )
        }
        .asState(false)

    fun setIsRegistering(registering: Boolean) {
        mIsRegistering.value = registering
    }

    suspend fun register() {
        executeWhileLoading {
            accountStub.create(userCreation {
                email = this@LoginViewModel.email.value
                username = this@LoginViewModel.username.value
            })
        }
    }

    suspend fun login() {
        executeWhileLoading {
            accountStub.sendConnectionEmail(email {
                email = this@LoginViewModel.email.value
            })
        }
    }

    suspend fun login(password: String) {
        executeWhileLoading {
            val token = accountStub.connect(connectionCredentials {
                email = this@LoginViewModel.email.value
                this.password = password
                client = defaultClient
            })
            preferences.storeAuthToken(token)
        }
    }

    private suspend fun executeWhileLoading(block: suspend () -> Unit) {
        try {
            mIsLoading.value = true
            block()
        } finally {
            mIsLoading.value = false
        }
    }

    private fun String.between(@IntegerRes a: Int, @IntegerRes b: Int) =
        isNotBlank() && length in resources.getInteger(a)..resources.getInteger(b)
}
