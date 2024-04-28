package app.fyreplace.fyreplace.events

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface EventsManager {
    val events: SharedFlow<Event>

    fun post(event: Event)
}

class EventsManagerImpl @Inject constructor() : EventsManager {
    private val scope = MainScope()
    private val mEvents = MutableSharedFlow<Event>()
    override val events = mEvents.asSharedFlow()

    override fun post(event: Event) {
        scope.launch { mEvents.emit(event) }
    }
}

interface Event
