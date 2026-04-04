package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import app.fyreplace.fyreplace.legacy.extensions.storeAuthToken
import app.fyreplace.fyreplace.legacy.grpc.defaultClient
import app.fyreplace.protos.AccountServiceClient
import app.fyreplace.protos.CommentServiceClient
import app.fyreplace.protos.ConnectionToken
import app.fyreplace.protos.Id
import app.fyreplace.protos.Token
import app.fyreplace.protos.UserServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import okio.ByteString
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class MainViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    private val accountService: AccountServiceClient,
    private val userService: UserServiceClient,
    private val commentService: CommentServiceClient
) : BaseViewModel() {
    suspend fun confirmActivation(tokenValue: String) =
        preferences.storeAuthToken(
            accountService.ConfirmActivation().executeFully(makeConnectionToken(tokenValue))
        )

    suspend fun confirmConnection(tokenValue: String) =
        preferences.storeAuthToken(
            accountService.ConfirmConnection().executeFully(makeConnectionToken(tokenValue))
        )

    suspend fun confirmEmailUpdate(tokenValue: String) =
        userService.ConfirmEmailUpdate().executeFully(Token(token = tokenValue))

    suspend fun acknowledgeComment(id: ByteString) =
        commentService.Acknowledge().executeFully(Id(id = id))

    private fun makeConnectionToken(tokenValue: String) = ConnectionToken(
        token = tokenValue,
        client = defaultClient
    )
}
