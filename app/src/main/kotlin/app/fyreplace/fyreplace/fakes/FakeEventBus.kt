package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeEventBus : EventBus {
    private val mStoredEvents = mutableListOf<Event>()

    override val events = MutableSharedFlow<Event>().asSharedFlow()
    val storedEvents: List<Event> = mStoredEvents

    override suspend fun publish(event: Event) {
        mStoredEvents.add(event)
    }
}
