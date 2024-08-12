package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.events.FailureEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(eventBus: EventBus) : ViewModelBase() {
    private val failures = MutableStateFlow(emptyList<FailureEvent>())

    val currentFailure = failures
        .map(List<FailureEvent>::firstOrNull)
        .asState(null)

    init {
        viewModelScope.launch {
            eventBus.events
                .filterIsInstance<FailureEvent>()
                .collect { failures.value += it }
        }
    }

    fun dismissError() {
        failures.value = failures.value.drop(1)
    }
}
