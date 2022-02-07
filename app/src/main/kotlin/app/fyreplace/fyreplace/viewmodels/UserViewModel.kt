package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("CheckResult")
class UserViewModel(
    initialProfile: Profile,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) :
    BaseViewModel() {
    private val mUser = MutableStateFlow<User?>(null)
    private val mBlocked = MutableStateFlow(initialProfile.isBlocked)
    private val mBanned = MutableStateFlow(initialProfile.isBanned)
    val user: Flow<User?> = mUser
    val blocked: Flow<Boolean> = mBlocked
    val banned: Flow<Boolean> = mBanned

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
}

enum class Sentence {
    WEEK,
    MONTH,
    PERMANENTLY
}
