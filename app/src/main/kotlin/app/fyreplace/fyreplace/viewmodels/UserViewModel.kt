package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceGrpcKt
import app.fyreplace.protos.stringId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("CheckResult")
class UserViewModel(private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub) :
    BaseViewModel() {
    private val mUser = MutableStateFlow<User?>(null)
    val user: Flow<User?> = mUser

    suspend fun retrieve(userId: String) {
        mUser.value = userStub.retrieve(stringId { id = userId })
    }

    suspend fun report() {
        userStub.report(stringId { id = mUser.value?.profile?.id ?: return })
    }
}
