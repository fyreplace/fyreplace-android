package app.fyreplace.fyreplace.viewmodels.events

import app.fyreplace.fyreplace.viewmodels.Event

interface PositionalEvent : Event {
    val position: Int
}

interface ItemEvent<I> : Event {
    val item: I

    fun atPosition(position: Int): ItemPositionalEvent<I> {
        return object : ItemPositionalEvent<I> {
            override val position = position
            override val item = this@ItemEvent.item
        }
    }
}

interface ItemPositionalEvent<I> : PositionalEvent, ItemEvent<I>
