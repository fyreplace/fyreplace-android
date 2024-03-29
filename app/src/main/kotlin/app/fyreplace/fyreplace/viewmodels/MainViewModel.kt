package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import app.fyreplace.fyreplace.extensions.storeAuthToken
import app.fyreplace.fyreplace.grpc.defaultClient
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class MainViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val accountStub: AccountServiceGrpcKt.AccountServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub
) : BaseViewModel() {
    suspend fun confirmActivation(tokenValue: String) =
        preferences.storeAuthToken(accountStub.confirmActivation(makeConnectionToken(tokenValue)))

    suspend fun confirmConnection(tokenValue: String) =
        preferences.storeAuthToken(accountStub.confirmConnection(makeConnectionToken(tokenValue)))

    suspend fun confirmEmailUpdate(tokenValue: String) {
        userStub.confirmEmailUpdate(token { token = tokenValue })
    }

    suspend fun acknowledgeComment(id: ByteString) {
        commentStub.acknowledge(id { this.id = id })
    }

    private fun makeConnectionToken(tokenValue: String) = connectionToken {
        token = tokenValue
        client = defaultClient
    }
}
