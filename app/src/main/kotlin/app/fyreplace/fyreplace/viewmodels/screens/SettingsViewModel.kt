package app.fyreplace.fyreplace.viewmodels.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.codePointCount
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    private val resourceResolver: ResourceResolver,
    storeResolver: StoreResolver,
    apiResolver: ApiResolver,
) : ApiViewModelBase(eventBus, storeResolver, apiResolver) {
    var currentUser by state.saveable { mutableStateOf<User?>(null) }
        private set
    var bio by state.saveable { mutableStateOf("") }
        private set
    var isLoadingAvatar by state.saveable { mutableStateOf(false) }
        private set
    val canUpdateBio
        get() = bio != currentUser?.bio.orEmpty() && bio.codePointCount <= resourceResolver.getInteger(
            R.integer.bio_max_length
        )

    init {
        loadCurrentUser()
    }

    fun updateAvatar(file: File) = call(apiResolver::users) {
        isLoadingAvatar = true

        val avatar = setCurrentUserAvatar(file).failWith {
            when (it.code) {
                413 -> Event.Failure(
                    R.string.settings_error_413_title,
                    R.string.settings_error_413_message
                )

                415 -> Event.Failure(
                    R.string.settings_error_415_title,
                    R.string.settings_error_415_message
                )

                else -> Event.Failure()
            }
        }

        isLoadingAvatar = false

        if (avatar != null) {
            currentUser = currentUser?.copy(avatar = avatar)
        }
    }

    fun removeAvatar() = call(apiResolver::users) {
        deleteCurrentUserAvatar().require() ?: return@call
        currentUser = currentUser?.copy(avatar = "")
    }

    fun updatePendingBio(bio: String) {
        this.bio = bio
    }

    fun updateBio() = call(apiResolver::users) {
        bio = setCurrentUserBio(bio).require() ?: return@call
        currentUser = currentUser?.copy(bio = bio)
    }

    fun logout() {
        viewModelScope.launch {
            storeResolver.connectionStore.update(Connection.Builder::clear)
            storeResolver.secretsStore.update(Secrets.Builder::clear)
        }
    }

    private fun loadCurrentUser() = call(apiResolver::users) {
        currentUser = getCurrentUser().require()

        if (bio.isEmpty()) {
            bio = currentUser?.bio.orEmpty()
        }
    }
}
