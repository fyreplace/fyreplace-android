package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver
) : ViewModelBase() {
    private val failures: StateFlow<List<Event.Failure>> =
        state.getStateFlow(::failures.name, emptyList())

    val snackbarEvents = eventBus.events.filterIsInstance<Event.Snackbar>()

    val isAuthenticated = storeResolver.secretsStore.data
        .map { !it.token.isEmpty }
        .asState(true)

    val isRegistering = storeResolver.accountStore.data
        .map { it.isRegistering }
        .asState(false)

    val currentFailure = failures
        .map(List<Event.Failure>::firstOrNull)
        .asState(null)

    init {
        viewModelScope.launch {
            eventBus.events
                .filterIsInstance<Event.Failure>()
                .collect { state[::failures.name] = failures.value + it }
        }
    }

    fun dismissError() {
        state[::failures.name] = failures.value.drop(1)
    }
}
