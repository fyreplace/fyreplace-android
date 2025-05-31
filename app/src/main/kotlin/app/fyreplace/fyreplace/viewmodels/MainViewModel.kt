package app.fyreplace.fyreplace.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    eventBus: EventBus,
    storeResolver: StoreResolver
) : ViewModelBase() {
    private val failures = mutableStateListOf<Event.Failure>()

    val events = eventBus.events

    val isAuthenticated by storeResolver.secretsStore.data
        .map { !it.token.isEmpty }
        .asState(true)

    val isWaitingForRandomCode by storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)

    val isRegistering by storeResolver.accountStore.data
        .map { it.isRegistering }
        .asState(false)

    val currentFailure get() = failures.firstOrNull()

    init {
        viewModelScope.launch {
            eventBus.events
                .filterIsInstance<Event.Failure>()
                .collect(failures::add)
        }
    }

    fun dismissError() {
        failures.removeAt(0)
    }
}
