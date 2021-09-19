package app.fyreplace.client.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.Email
import app.fyreplace.protos.IntId
import app.fyreplace.protos.UserServiceGrpc
import com.google.protobuf.Empty

class SettingsViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val userStub: UserServiceGrpc.UserServiceStub,
    private val preferences: SharedPreferences
) : BaseViewModel() {
    suspend fun sendEmailUpdateEmail(address: String) {
        val request = Email.newBuilder()
            .setEmail(address)
            .build()
        awaitSingleResponse(userStub::sendEmailUpdateEmail, request)
    }

    suspend fun logout() {
        awaitSingleResponse(accountStub::disconnect, IntId.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        awaitSingleResponse(accountStub::delete, Empty.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }
}
