package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import app.fyreplace.fyreplace.legacy.extensions.isNotNullOrBlank
import app.fyreplace.protos.Image
import app.fyreplace.protos.User
import app.fyreplace.protos.UserServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CentralViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    private val userService: UserServiceClient
) : BaseViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mIsAuthenticated = MutableStateFlow(false)
    private val mCurrentUser = MutableStateFlow<User?>(null)
    val isAuthenticated = mIsAuthenticated.asStateFlow()
    val currentUser = mCurrentUser.asStateFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(preferences, "auth.token")
    }

    override fun onCleared() {
        super.onCleared()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == "auth.token") {
            mIsAuthenticated.value = sharedPreferences.getString(key, null).isNotNullOrBlank()
        }
    }

    suspend fun retrieveMe() {
        mCurrentUser.value = if (isAuthenticated.value) userService.RetrieveMe().executeFully(Unit)
        else null
    }

    fun setAvatar(image: Image) {
        mCurrentUser.value = mCurrentUser.value?.copy(
            profile = currentUser.value?.profile?.copy(avatar = image)
        )
    }
}
