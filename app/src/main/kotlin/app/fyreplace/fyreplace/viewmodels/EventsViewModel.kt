package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor() : ViewModel() {
    private val mEvents = MutableSharedFlow<Event>()
    val events = mEvents.asSharedFlow()

    fun post(event: Event) {
        viewModelScope.launch { mEvents.emit(event) }
    }
}

interface Event
