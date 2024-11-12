package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    eventBus: EventBus,
    resourceResolver: ResourceResolver,
    storeResolver: StoreResolver,
    private val apiResolver: ApiResolver,
) : ApiViewModelBase(eventBus, storeResolver) {
    val currentUser: StateFlow<User?> =
        state.getStateFlow(::currentUser.name, null)
    val bio: StateFlow<String> =
        state.getStateFlow(::bio.name, "")
    val isLoadingAvatar: StateFlow<Boolean> =
        state.getStateFlow(::isLoadingAvatar.name, false)
    val canUpdateBio = bio
        .combine(currentUser) { bio, currentUser ->
            bio != currentUser?.bio.orEmpty() && bio.codePointCount <= resourceResolver.getInteger(R.integer.bio_max_length)
        }
        .asState(false)


    fun loadCurrentUser() = call(apiResolver::users) {
        state[::currentUser.name] = getCurrentUser().require()
        state[::bio.name] = currentUser.value?.bio.orEmpty()
    }

    fun updateAvatar(file: File) = call(apiResolver::users) {
        state[::isLoadingAvatar.name] = true

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

        state[::isLoadingAvatar.name] = false

        if (avatar != null) {
            state[::currentUser.name] = currentUser.value?.copy(avatar = avatar)
        }
    }

    fun removeAvatar() = call(apiResolver::users) {
        deleteCurrentUserAvatar().require() ?: return@call
        state[::currentUser.name] = currentUser.value?.copy(avatar = "")
    }

    fun updatePendingBio(bio: String) {
        state[::bio.name] = bio
    }

    fun updateBio() = call(apiResolver::users) {
        state[::bio.name] = setCurrentUserBio(bio.value).require() ?: return@call
        state[::currentUser.name] = currentUser.value?.copy(bio = bio.value)
    }

    fun logout() {
        viewModelScope.launch {
            storeResolver.connectionStore.update(Connection.Builder::clear)
            storeResolver.secretsStore.update(Secrets.Builder::clear)
        }
    }
}
