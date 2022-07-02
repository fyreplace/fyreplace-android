package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("CheckResult")
class UserViewModel @AssistedInject constructor(
    @Assisted initialProfile: Profile,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel() {
    private val mUser = MutableStateFlow<User?>(null)
    private val mBlocked = MutableStateFlow(initialProfile.isBlocked)
    private val mBanned = MutableStateFlow(initialProfile.isBanned)
    val user = mUser.asStateFlow()
    val blocked = mBlocked.asStateFlow()
    val banned = mBanned.asStateFlow()

    suspend fun retrieve(userId: ByteString) {
        val newUser = userStub.retrieve(id { id = userId })
        mUser.value = newUser
        mBlocked.value = newUser.profile.isBlocked
        mBanned.value = newUser.profile.isBanned
    }

    suspend fun updateBlock(blocked: Boolean) {
        userStub.updateBlock(block {
            id = mUser.value?.profile?.id ?: return
            this.blocked = blocked
        })
        mBlocked.value = blocked
    }

    suspend fun report() {
        userStub.report(id { id = mUser.value?.profile?.id ?: return })
    }

    suspend fun ban(sentence: Sentence) {
        userStub.ban(banSentence {
            id = mUser.value?.profile?.id ?: return
            days = when (sentence) {
                Sentence.WEEK -> 7
                Sentence.MONTH -> 30
                Sentence.PERMANENTLY -> 0
            }
        })
        mBanned.value = true
    }

    companion object {
        fun provideFactory(
            assistedFactory: UserViewModelFactory,
            profile: Profile
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
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
