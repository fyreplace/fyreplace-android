package app.fyreplace.client.viewmodels

import android.content.res.Resources
import android.util.Log
import androidx.annotation.IntegerRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.fyreplace.client.R
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.Client
import app.fyreplace.protos.Credentials
import app.fyreplace.protos.UserCreation
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KFunction2

class LoginViewModel(
    private val resources: Resources,
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
) : ViewModel() {
    private val mIsRegistering = MutableLiveData(false)
    private val mCanProceed = MediatorLiveData<Boolean>()
    private var mIsLoading = MutableLiveData(false)
    val isRegistering: LiveData<Boolean> = mIsRegistering
    val email = MutableLiveData("")
    val username = MutableLiveData("")
    val password = MutableLiveData("")
    val canProceed: LiveData<Boolean> = mCanProceed
    val isLoading: LiveData<Boolean> = mIsLoading

    init {
        for (source in listOf(isRegistering, email, username, password)) {
            mCanProceed.addSource(source) { computeCanProceed() }
        }
    }

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
            .setClient(Client.newBuilder().setHardware("mobile").setSoftware("android"))
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

    private fun computeCanProceed() {
        val emailValue = email.value.orEmpty()
        val isEmailValid = emailValue.isNotBlank() && emailValue.length.between(
            R.integer.login_email_min_size,
            R.integer.login_email_max_size
        )

        val usernameValue = username.value.orEmpty()
        val isUsernameValid = usernameValue.isNotBlank() && usernameValue.length.between(
            R.integer.login_username_min_size,
            R.integer.login_username_max_size
        )

        val passwordValue = password.value.orEmpty()
        val isPasswordValid = passwordValue.isNotBlank() && passwordValue.length.between(
            R.integer.login_password_min_size,
            R.integer.login_password_max_size
        )
        mCanProceed.value =
            (!isRegistering.value!! || isEmailValid) && isUsernameValid && isPasswordValid
    }

    private fun Int.between(@IntegerRes a: Int, @IntegerRes b: Int) =
        this in resources.getInteger(a)..resources.getInteger(b)
}
