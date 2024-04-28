package app.fyreplace.fyreplace.events

abstract class ItemEvent<I>(val item: I) : Event {
    fun at(position: Int) = PositionalEvent(this, position)
}

class PositionalEvent<I>(val event: ItemEvent<I>, val position: Int) : Event
