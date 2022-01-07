package app.fyreplace.fyreplace.viewmodels

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.IntegerRes
import androidx.core.content.edit
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.defaultClient
import app.fyreplace.protos.*
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
    val password = MutableStateFlow("")
    val isLoading: StateFlow<Boolean> = mIsLoading
    val canProceed = isRegistering
        .combine(email) { registering, email ->
            !registering || email.between(
                R.integer.login_email_min_size,
                R.integer.login_email_max_size
            )
        }
        .combine(username) { res, username ->
            res && username.between(
                R.integer.login_username_min_size,
                R.integer.login_username_max_size
            )
        }
        .combine(password) { res, password ->
            res && password.between(
                R.integer.login_password_min_size,
                R.integer.login_password_max_size
            )
        }.asState(false)

    fun setIsRegistering(registering: Boolean) {
        mIsRegistering.value = registering
    }

    suspend fun register() {
        executeWhileLoading {
            accountStub.create(userCreation {
                email = this@LoginViewModel.email.value
                username = this@LoginViewModel.username.value
                password = this@LoginViewModel.password.value
            })
        }
    }

    suspend fun login() {
        executeWhileLoading {
            val response = accountStub.connect(credentials {
                identifier = this@LoginViewModel.username.value
                password = this@LoginViewModel.password.value
                client = defaultClient
            })
            preferences.edit { putString("auth.token", response.token) }
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
