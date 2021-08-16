package app.fyreplace.client.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.ConnectionToken
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val preferences: SharedPreferences
) : ViewModel() {
    private val mPageChoices = MutableStateFlow<List<@StringRes Int>>(emptyList())
    private val mCurrentPage = MutableStateFlow(0)
    private var lastIntent: Intent? = null
    val hasPageChoices = mPageChoices
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val pageState = mPageChoices
        .combine(mCurrentPage) { choices, page -> PagesState(choices, page) }
        .stateIn(viewModelScope, SharingStarted.Lazily, PagesState(emptyList(), 0))

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
}

data class PagesState(val choices: List<Int>, val current: Int = 0)
