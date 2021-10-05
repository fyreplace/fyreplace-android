package app.fyreplace.client.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.client.grpc.awaitImageUpload
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.*
import com.google.protobuf.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val preferences: SharedPreferences,
    private val accountBlocking: AccountServiceGrpc.AccountServiceBlockingStub,
    private val userBlocking: UserServiceGrpc.UserServiceBlockingStub,
    private val userStub: UserServiceGrpc.UserServiceStub
) : BaseViewModel() {
    suspend fun confirmActivation(token: String) {
        val request = ConnectionToken.newBuilder()
            .setToken(token)
            .setClient(defaultClient)
            .build()

        withContext(Dispatchers.IO) {
            val response = accountBlocking.confirmActivation(request)
            preferences.edit { putString("auth.token", response.token) }
        }
    }

    suspend fun updateAvatar(image: ByteArray?) = awaitImageUpload(userStub::updateAvatar, image)

    suspend fun updatePassword(password: String) {
        val request = Password.newBuilder().setPassword(password).build()
        withContext(Dispatchers.IO) { userBlocking.updatePassword(request) }
    }

    suspend fun sendEmailUpdateEmail(address: String) {
        val request = Email.newBuilder().setEmail(address).build()
        withContext(Dispatchers.IO) { userBlocking.sendEmailUpdateEmail(request) }
    }

    suspend fun confirmEmailUpdate(token: String) {
        val request = Token.newBuilder().setToken(token).build()
        withContext(Dispatchers.IO) { userBlocking.confirmEmailUpdate(request) }
    }

    suspend fun updateBio(bio: String) {
        val request = Bio.newBuilder().setBio(bio).build()
        withContext(Dispatchers.IO) { userBlocking.updateBio(request) }
    }

    @SuppressLint("CheckResult")
    suspend fun logout() = withContext(Dispatchers.IO) {
        accountBlocking.disconnect(StringId.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }

    @SuppressLint("CheckResult")
    suspend fun delete() = withContext(Dispatchers.IO) {
        accountBlocking.delete(Empty.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }
}
