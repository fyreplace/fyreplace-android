package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.ItemPositionalEvent
import app.fyreplace.fyreplace.events.PositionalEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class DynamicListViewModel<Item>(val em: EventsManager) : BaseViewModel() {
    abstract val addedItems: Flow<ItemPositionalEvent<Item>>
    abstract val updatedItems: Flow<ItemPositionalEvent<Item>>
    abstract val removedItems: Flow<PositionalEvent>
    private val eventJobs = mutableListOf<Job>()

    abstract fun addItem(position: Int, item: Item)

    abstract fun updateItem(position: Int, item: Item)

    abstract fun removeItem(position: Int)

    fun refreshEventHandlers() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()

        eventJobs.add(viewModelScope.launch {
            addedItems.collect { addItem(it.position, it.item) }
        })
        eventJobs.add(viewModelScope.launch {
            updatedItems.collect { updateItem(it.position, it.item) }
        })
        eventJobs.add(viewModelScope.launch {
            removedItems.collect { removeItem(it.position) }
        })
    }
}
