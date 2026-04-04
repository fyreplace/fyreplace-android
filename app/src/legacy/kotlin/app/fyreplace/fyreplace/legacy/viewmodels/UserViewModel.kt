package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.BanSentence
import app.fyreplace.protos.Block
import app.fyreplace.protos.Id
import app.fyreplace.protos.Profile
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.ByteString

@SuppressLint("CheckResult")
class UserViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    private val userService: UserServiceClient,
    @Assisted initialProfile: Profile
) : BaseViewModel() {
    private val mUser = MutableStateFlow<User?>(null)
    private val mBlocked = MutableStateFlow(initialProfile.is_blocked)
    private val mBanned = MutableStateFlow(initialProfile.is_banned)
    val user = mUser.asStateFlow()
    val blocked = mBlocked.asStateFlow()
    val banned = mBanned.asStateFlow()

    suspend fun retrieve(userId: ByteString) {
        val newUser = userService.Retrieve().executeFully(Id(id = userId))
        mUser.value = newUser
        mBlocked.value = newUser.profile?.is_blocked == true
        mBanned.value = newUser.profile?.is_banned == true
    }

    suspend fun updateBlock(blocked: Boolean) {
        userService.UpdateBlock().executeFully(
            Block(
                id = mUser.value?.profile?.id ?: return,
                blocked = blocked
            )
        )
        mBlocked.value = blocked
    }

    suspend fun report() {
        userService.Report().executeFully(Id(id = mUser.value?.profile?.id ?: return))
    }

    suspend fun ban(sentence: Sentence) {
        userService.Ban().executeFully(
            BanSentence(
                id = mUser.value?.profile?.id ?: return,
                days = when (sentence) {
                    Sentence.WEEK -> 7
                    Sentence.MONTH -> 30
                    Sentence.PERMANENTLY -> 0
                }
            )
        )
        mBanned.value = true
    }

    companion object {
        fun provideFactory(assistedFactory: UserViewModelFactory, profile: Profile) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    assistedFactory.create(profile) as T
            }
    }
}

enum class Sentence {
    WEEK,
    MONTH,
    PERMANENTLY
}
