package app.fyreplace.client.viewmodels

import android.content.SharedPreferences
import app.fyreplace.client.grpc.awaitSingleResponse
import app.fyreplace.client.isNotNullOrBlank
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceGrpc
import com.google.protobuf.Empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CentralViewModel(
    private val userStub: UserServiceGrpc.UserServiceStub,
    private val preferences: SharedPreferences
) : BaseViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mIsAuthenticated = MutableStateFlow(false)
    private val mUser = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = mUser
    val isAuthenticated: StateFlow<Boolean> = mIsAuthenticated

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(preferences, "auth.token")
    }

    override fun onCleared() {
        super.onCleared()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "auth.token") {
            mIsAuthenticated.value = sharedPreferences.getString(key, null).isNotNullOrBlank()
        }
    }

    suspend fun retrieveMe() {
        mUser.value = if (isAuthenticated.value) awaitSingleResponse(
            userStub::retrieveMe,
            Empty.getDefaultInstance()
        ) else null
    }
}
