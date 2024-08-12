package app.fyreplace.fyreplace.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class EventBus {
    private val mEvents = MutableSharedFlow<Event>()

    val events = mEvents.asSharedFlow()

    suspend fun publish(event: Event) = mEvents.emit(event)
}
