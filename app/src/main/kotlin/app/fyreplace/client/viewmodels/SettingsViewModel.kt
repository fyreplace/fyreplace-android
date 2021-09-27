package app.fyreplace.client.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.client.grpc.awaitImageUpload
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.*
import com.google.protobuf.Empty

class SettingsViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val userStub: UserServiceGrpc.UserServiceStub,
    private val preferences: SharedPreferences
) : BaseViewModel() {
    suspend fun confirmActivation(token: String) {
        val request = ConnectionToken.newBuilder()
            .setToken(token)
            .setClient(defaultClient)
            .build()

        val response = awaitSingleResponse(accountStub::confirmActivation, request)
        preferences.edit { putString("auth.token", response.token) }
    }

    suspend fun updateAvatar(image: ByteArray?) = awaitImageUpload(userStub::updateAvatar, image)

    suspend fun updatePassword(password: String) {
        val request = Password.newBuilder().setPassword(password).build()
        awaitSingleResponse(userStub::updatePassword, request)
    }

    suspend fun sendEmailUpdateEmail(address: String) {
        val request = Email.newBuilder().setEmail(address).build()
        awaitSingleResponse(userStub::sendEmailUpdateEmail, request)
    }

    suspend fun confirmEmailUpdate(token: String) {
        val request = Token.newBuilder().setToken(token).build()
        awaitSingleResponse(userStub::confirmEmailUpdate, request)
    }

    suspend fun updateBio(bio: String) {
        val request = Bio.newBuilder().setBio(bio).build()
        awaitSingleResponse(userStub::updateBio, request)
    }

    suspend fun logout() {
        awaitSingleResponse(accountStub::disconnect, StringId.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        awaitSingleResponse(accountStub::delete, Empty.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }
}
