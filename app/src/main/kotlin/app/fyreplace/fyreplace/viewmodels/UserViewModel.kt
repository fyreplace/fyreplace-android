package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.*
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
    val user: Flow<User?> = mUser
    val blocked: Flow<Boolean> = mBlocked

    suspend fun retrieve(userId: String) {
        val newUser = userStub.retrieve(stringId { id = userId })
        mUser.value = newUser
        mBlocked.value = newUser.profile.isBlocked
    }

    suspend fun updateBlock(blocked: Boolean) {
        userStub.updateBlock(block {
            id = mUser.value?.profile?.id ?: return
            this.blocked = blocked
        })
        mBlocked.value = blocked
    }

    suspend fun report() {
        userStub.report(stringId { id = mUser.value?.profile?.id ?: return })
    }
}
