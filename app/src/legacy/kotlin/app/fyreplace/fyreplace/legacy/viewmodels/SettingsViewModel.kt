package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.UserWasBlockedEvent
import app.fyreplace.fyreplace.legacy.events.UserWasUnblockedEvent
import app.fyreplace.fyreplace.legacy.extensions.imageChunks
import app.fyreplace.fyreplace.legacy.extensions.sendAllAndClose
import app.fyreplace.protos.AccountServiceClient
import app.fyreplace.protos.Email
import app.fyreplace.protos.Id
import app.fyreplace.protos.Image
import app.fyreplace.protos.UserServiceClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@SuppressLint("CheckResult")
class SettingsViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val accountService: AccountServiceClient,
    private val userService: UserServiceClient,
    @Assisted initialBlockedUsers: Int,
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

    suspend fun updateAvatar(image: ByteArray?): Image {
        val (sender, receiver) = userService.UpdateAvatar().executeFully()
        sender.sendAllAndClose(image.imageChunks)
        return receiver.receive()
    }

    suspend fun sendEmailUpdateEmail(address: String) {
        userService.SendEmailUpdateEmail().executeFully(Email(email = address))
    }

    suspend fun logout() {
        accountService.Disconnect().executeFully(Id())
        preferences.edit { putString("auth.token", "") }
    }

    suspend fun delete() {
        accountService.Delete().executeFully(Unit)
        preferences.edit { putString("auth.token", "") }
    }

    companion object {
        fun provideFactory(assistedFactory: SettingsViewModelFactory, blockedUsers: Int) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    assistedFactory.create(blockedUsers) as T
            }
    }
}
