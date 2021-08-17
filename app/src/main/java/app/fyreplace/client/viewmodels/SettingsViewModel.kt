package app.fyreplace.client.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.IntId

class SettingsViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val preferences: SharedPreferences
) : BaseViewModel() {
    suspend fun logout() {
        awaitSingleResponse(accountStub::disconnect, IntId.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }
}
