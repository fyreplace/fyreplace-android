package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.ItemEvent
import com.google.protobuf.ByteString
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class DynamicListViewModel<Item>(val em: EventsManager) : BaseViewModel() {
    abstract val addedItems: Flow<ItemEvent<Item>>
    abstract val updatedItems: Flow<ItemEvent<Item>>
    abstract val removedItems: Flow<ItemEvent<Item>>
    private val eventJobs = mutableListOf<Job>()

    abstract fun getPosition(item: Item): Int

    abstract fun getItemId(item: Item): ByteString

    abstract fun addItem(position: Int, item: Item)

    abstract fun updateItem(position: Int, item: Item)

    abstract fun removeItem(position: Int, item: Item)

    fun refreshEventHandlers() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()

        eventJobs.add(viewModelScope.launch {
            addedItems.collect {
                var position = getPosition(it.item)

                if (position == -1) {
                    position = 0
                }

                addItem(position, it.item)
            }
        })

        eventJobs.add(viewModelScope.launch {
            updatedItems.collect {
                val position = getPosition(it.item)

                if (position != -1) {
                    updateItem(position, it.item)
                }
            }
        })

        eventJobs.add(viewModelScope.launch {
            removedItems.collect {
                val position = getPosition(it.item)

                if (position != -1) {
                    removeItem(position, it.item)
                }
            }
        })
    }
}
