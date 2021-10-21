package app.fyreplace.client.viewmodels

import android.content.SharedPreferences
import app.fyreplace.client.isNotNullOrBlank
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceGrpcKt
import com.google.protobuf.empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CentralViewModel(
    private val preferences: SharedPreferences,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
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
        mUser.value =
            if (isAuthenticated.value) userStub.retrieveMe(empty { }) else null
    }
}
