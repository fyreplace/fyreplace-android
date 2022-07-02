package app.fyreplace.fyreplace.viewmodels

import android.content.SharedPreferences
import app.fyreplace.fyreplace.isNotNullOrBlank
import app.fyreplace.protos.Image
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceGrpcKt
import app.fyreplace.protos.copy
import com.google.protobuf.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CentralViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) : BaseViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mIsAuthenticated = MutableStateFlow(false)
    private val mCurrentUser = MutableStateFlow<User?>(null)
    private val mBlockedUsers = MutableStateFlow(0)
    val isAuthenticated = mIsAuthenticated.asStateFlow()
    val currentUser = mCurrentUser.asStateFlow()
    val blockedUsers = mBlockedUsers.asStateFlow()

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
        mCurrentUser.value =
            if (isAuthenticated.value) userStub.retrieveMe(Empty.getDefaultInstance()) else null
        mBlockedUsers.value = currentUser.value?.blockedUsers ?: 0
    }

    fun setAvatar(image: Image) {
        mCurrentUser.value = mCurrentUser.value?.copy { profile = profile.copy { avatar = image } }
    }

    fun addBlockedUser() {
        mBlockedUsers.value = mBlockedUsers.value + 1
    }

    fun removeBlockedUser() {
        mBlockedUsers.value = mBlockedUsers.value - 1
    }
}
