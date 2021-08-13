package app.fyreplace.client.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.*
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.grpc.defaultClient
import app.fyreplace.protos.AccountServiceGrpc
import app.fyreplace.protos.ConnectionToken

class MainViewModel(
    private val accountStub: AccountServiceGrpc.AccountServiceStub,
    private val preferences: SharedPreferences
) : ViewModel() {
    private val mPageChoices = MutableLiveData<List<@StringRes Int>>(emptyList())
    private val mCurrentPage = MutableLiveData(0)
    private val mPagesState = MediatorLiveData<PagesState>()
    private var lastIntent: Intent? = null
    val pageState = mPagesState
    val hasPageChoices = mPageChoices.map { it.isNotEmpty() }

    init {
        mPagesState.addSource(mPageChoices.distinctUntilChanged()) {
            mPagesState.value = PagesState(it, 0)
        }

        mPagesState.addSource(mCurrentPage.distinctUntilChanged()) {
            mPagesState.value = PagesState(mPageChoices.value!!, it)
        }
    }

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
