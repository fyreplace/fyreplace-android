package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.fyreplace.extensions.imageChunkFlow
import app.fyreplace.protos.*
import com.google.protobuf.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class SettingsViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel() {
    suspend fun updateAvatar(image: ByteArray?) = userStub.updateAvatar(image.imageChunkFlow)

    suspend fun sendEmailUpdateEmail(address: String) {
        userStub.sendEmailUpdateEmail(email { email = address })
    }

    suspend fun updateBio(bioValue: String) {
        userStub.updateBio(bio { bio = bioValue })
    }

    suspend fun logout() {
        accountStub.disconnect(id { })
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        accountStub.delete(Empty.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }
}
