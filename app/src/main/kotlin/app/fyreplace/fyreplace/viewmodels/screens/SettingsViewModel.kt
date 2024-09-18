package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.StoreResolver
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
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    apiResolver: ApiResolver
) : ApiViewModelBase(eventBus, storeResolver) {
    val currentUser: StateFlow<User?> =
        state.getStateFlow(::currentUser.name, null)

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

    fun logout() {
        viewModelScope.launch {
            storeResolver.connectionStore.update(Connection.Builder::clear)
            storeResolver.secretsStore.update(Secrets.Builder::clear)
        }
    }
}
