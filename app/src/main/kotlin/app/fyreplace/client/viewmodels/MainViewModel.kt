package app.fyreplace.client.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.ConnectionToken
import app.fyreplace.protos.Token
import app.fyreplace.protos.UserServiceGrpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MainViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val userStub: UserServiceGrpc.UserServiceStub,
    private val preferences: SharedPreferences
) : BaseViewModel() {
    private val mPageChoices = MutableStateFlow<List<@StringRes Int>>(emptyList())
    private val mCurrentPage = MutableStateFlow(0)
    private var lastIntent: Intent? = null
    val hasPageChoices = mPageChoices
        .map { it.isNotEmpty() }
        .asState(false)
    val pageState = mPageChoices
        .combine(mCurrentPage) { choices, page -> PagesState(choices, page) }
        .asState(PagesState(emptyList(), 0))

    fun setPageChoices(choices: List<Int>) {
        mPageChoices.value = choices
    }

    fun choosePage(page: Int) {
        mCurrentPage.value = page
    }

    fun getUsableIntent(intent: Intent?) =
        if (intent != lastIntent) intent.also { lastIntent = it } else null

    suspend fun confirmActivation(token: String) {
        val request = ConnectionToken.newBuilder()
            .setToken(token)
            .setClient(defaultClient)
            .build()

        val response = awaitSingleResponse(accountStub::confirmActivation, request)
        preferences.edit { putString("auth.token", response.token) }
    }

    suspend fun confirmEmailUpdate(token: String) {
        val request = Token.newBuilder()
            .setToken(token)
            .build()
        awaitSingleResponse(userStub::confirmEmailUpdate, request)
    }
}

data class PagesState(val choices: List<Int>, val current: Int = 0)
