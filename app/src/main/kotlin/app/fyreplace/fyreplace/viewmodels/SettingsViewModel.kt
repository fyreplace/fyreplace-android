package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.UserWasBlockedEvent
import app.fyreplace.fyreplace.events.UserWasUnblockedEvent
import app.fyreplace.fyreplace.extensions.imageChunkFlow
import app.fyreplace.protos.*
import com.google.protobuf.Empty
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@SuppressLint("CheckResult")
class SettingsViewModel @AssistedInject constructor(
    @Assisted initialBlockedUsers: Int,
    em: EventsManager,
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel() {
    private var mBlockedUsers = MutableStateFlow(initialBlockedUsers)
    val blockedUsers = mBlockedUsers.asStateFlow()

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<UserWasBlockedEvent>().collect { mBlockedUsers.value++ }
        }
        viewModelScope.launch {
            em.events.filterIsInstance<UserWasUnblockedEvent>().collect { mBlockedUsers.value-- }
        }
    }

    suspend fun updateAvatar(image: ByteArray?) = userStub.updateAvatar(image.imageChunkFlow)

    suspend fun sendEmailUpdateEmail(address: String) {
        userStub.sendEmailUpdateEmail(email { email = address })
    }

    suspend fun logout() {
        accountStub.disconnect(id { })
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        accountStub.delete(Empty.getDefaultInstance())
        preferences.edit { putString("auth.token", "") }
    }

    companion object {
        fun provideFactory(
            assistedFactory: SettingsViewModelFactory,
            blockedUsers: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(blockedUsers) as T
        }
    }
}
