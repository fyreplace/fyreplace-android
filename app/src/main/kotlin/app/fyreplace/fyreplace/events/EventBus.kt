package app.fyreplace.fyreplace.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

interface EventBus {
    val events: SharedFlow<Event>

    suspend fun publish(event: Event)
}

class HotEventBus @Inject constructor() : EventBus {
    private val mEvents = MutableSharedFlow<Event>()

    override val events = mEvents.asSharedFlow()

    override suspend fun publish(event: Event) = mEvents.emit(event)
}
