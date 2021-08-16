package app.fyreplace.client.viewmodels

import android.content.res.Resources
import androidx.annotation.IntegerRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fyreplace.client.R
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.Credentials
import app.fyreplace.protos.UserCreation
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.*
import kotlin.reflect.KFunction2

class LoginViewModel(
    private val resources: Resources,
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
) : ViewModel() {
    private val mIsRegistering = MutableStateFlow(false)
    private var mIsLoading = MutableStateFlow(false)
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
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setIsRegistering(registering: Boolean) {
        mIsRegistering.value = registering
    }

    suspend fun register() {
        val request = UserCreation.newBuilder()
            .setEmail(email.value)
            .setUsername(username.value)
            .setPassword(password.value)
            .build()

        awaitResponse(accountStub::create, request)
    }

    suspend fun login() {
        val request = Credentials.newBuilder()
            .setIdentifier(username.value)
            .setPassword(password.value)
            .setClient(defaultClient)
            .build()

        awaitResponse(accountStub::connect, request)
    }

    private suspend fun <Request, Response> awaitResponse(
        call: KFunction2<Request, StreamObserver<Response>, Unit>,
        request: Request
    ) {
        try {
            mIsLoading.value = true
            awaitSingleResponse(call, request)
        } finally {
            mIsLoading.value = false
        }
    }

    private fun String.between(@IntegerRes a: Int, @IntegerRes b: Int) =
        isNotBlank() && length in resources.getInteger(a)..resources.getInteger(b)
}
