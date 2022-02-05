package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.fyreplace.data.imageChunkFlow
import app.fyreplace.fyreplace.grpc.defaultClient
import app.fyreplace.protos.*
import com.google.protobuf.empty

@SuppressLint("CheckResult")
class SettingsViewModel(
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel() {
    suspend fun confirmActivation(tokenValue: String) =
        storeToken(accountStub.confirmActivation(makeConnectionToken(tokenValue)))

    suspend fun confirmConnection(tokenValue: String) =
        storeToken(accountStub.confirmConnection(makeConnectionToken(tokenValue)))

    suspend fun updateAvatar(image: ByteArray?) {
        userStub.updateAvatar(image.imageChunkFlow())
    }

    suspend fun sendEmailUpdateEmail(address: String) {
        userStub.sendEmailUpdateEmail(email { email = address })
    }

    suspend fun confirmEmailUpdate(tokenValue: String) {
        userStub.confirmEmailUpdate(token { token = tokenValue })
    }

    suspend fun updateBio(bioValue: String) {
        userStub.updateBio(bio { bio = bioValue })
    }

    suspend fun logout() {
        accountStub.disconnect(id { })
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        accountStub.delete(empty { })
        preferences.edit { putString("auth.token", "") }
    }

    private fun makeConnectionToken(tokenValue: String) = connectionToken {
        token = tokenValue
        client = defaultClient
    }

    private fun storeToken(token: Token) = preferences.edit { putString("auth.token", token.token) }
}
