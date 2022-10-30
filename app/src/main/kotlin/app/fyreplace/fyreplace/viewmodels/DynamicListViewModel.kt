package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.ItemEvent
import app.fyreplace.fyreplace.events.PositionalEvent
import com.google.protobuf.ByteString
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max

abstract class DynamicListViewModel<Item>(val em: EventsManager) : BaseViewModel() {
    protected abstract val addedItems: Flow<ItemEvent<Item>>
    protected abstract val updatedItems: Flow<ItemEvent<Item>>
    protected abstract val removedItems: Flow<ItemEvent<Item>>
    private val mAddedPositions = MutableSharedFlow<PositionalEvent<Item>>()
    private val mUpdatedPositions = MutableSharedFlow<PositionalEvent<Item>>()
    private val mRemovedPositions = MutableSharedFlow<PositionalEvent<Item>>()
    private val eventJobs = mutableListOf<Job>()
    val addedPositions = mAddedPositions.asSharedFlow()
    val updatedPositions = mUpdatedPositions.asSharedFlow()
    val removedPositions = mRemovedPositions.asSharedFlow()

    abstract fun getPosition(item: Item): Int

    abstract fun getItemId(item: Item): ByteString

    abstract fun addItem(position: Int, item: Item)

    abstract fun updateItem(position: Int, item: Item)

    abstract fun removeItem(position: Int, item: Item)

    fun refreshEventHandlers() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()

        eventJobs.add(viewModelScope.launch {
            addedItems
                .map { it.at(max(getPosition(it.item), 0)) }
                .collect {
                    addItem(it.position, it.event.item)
                    mAddedPositions.emit(it)
                }
        })

        eventJobs.add(viewModelScope.launch {
            updatedItems
                .map { it.at(getPosition(it.item)) }
                .filter { it.position != -1 }
                .collect {
                    updateItem(it.position, it.event.item)
                    mUpdatedPositions.emit(it)
                }
        })

        eventJobs.add(viewModelScope.launch {
            removedItems
                .map { it.at(getPosition(it.item)) }
                .filter { it.position != -1 }
                .collect {
                    removeItem(it.position, it.event.item)
                    mRemovedPositions.emit(it)
                }
        })
    }
}
