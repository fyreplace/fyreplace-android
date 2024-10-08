package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    private val apiResolver: ApiResolver,
) : ApiViewModelBase(eventBus, storeResolver) {
    val currentUser: StateFlow<User?> =
        state.getStateFlow(::currentUser.name, null)
    val isLoadingAvatar: StateFlow<Boolean> =
        state.getStateFlow(::isLoadingAvatar.name, false)

    init {
        viewModelScope.launch {
            storeResolver.secretsStore.data
                .map { it.token }
                .distinctUntilChanged()
                .filter { !it.isEmpty }
                .collect {
                    call(apiResolver::users) {
                        state[::currentUser.name] = getCurrentUser().require()
                    }
                }
        }
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
        deleteCurrentUserAvatar().require()
        state[::currentUser.name] = currentUser.value?.copy(avatar = "")
    }

    fun logout() {
        viewModelScope.launch {
            storeResolver.connectionStore.update(Connection.Builder::clear)
            storeResolver.secretsStore.update(Secrets.Builder::clear)
        }
    }
}
