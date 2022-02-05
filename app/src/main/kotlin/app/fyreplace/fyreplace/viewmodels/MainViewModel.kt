package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.fyreplace.grpc.defaultClient
import app.fyreplace.protos.*

@SuppressLint("CheckResult")
class MainViewModel(
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel() {
    suspend fun confirmActivation(tokenValue: String) =
        storeToken(accountStub.confirmActivation(makeConnectionToken(tokenValue)))

    suspend fun confirmConnection(tokenValue: String) =
        storeToken(accountStub.confirmConnection(makeConnectionToken(tokenValue)))

    suspend fun confirmEmailUpdate(tokenValue: String) {
        userStub.confirmEmailUpdate(token { token = tokenValue })
    }

    private fun makeConnectionToken(tokenValue: String) = connectionToken {
        token = tokenValue
        client = defaultClient
    }

    private fun storeToken(token: Token) = preferences.edit { putString("auth.token", token.token) }
}
