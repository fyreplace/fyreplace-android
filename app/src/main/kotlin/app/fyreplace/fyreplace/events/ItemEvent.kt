package app.fyreplace.fyreplace.events

abstract class ItemEvent<I>(val item: I) : Event {
    fun at(position: Int) = PositionalEvent(item, position)
}

class PositionalEvent<I>(item: I, val position: Int) : ItemEvent<I>(item)
