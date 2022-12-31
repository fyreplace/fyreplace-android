package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.ItemEvent
import app.fyreplace.fyreplace.events.PositionalEvent
import com.google.protobuf.ByteString
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    abstract fun addItem(event: PositionalEvent<Item>)

    abstract fun updateItem(event: PositionalEvent<Item>)

    abstract fun removeItem(event: PositionalEvent<Item>)

    fun refreshEventHandlers() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()

        eventJobs.add(viewModelScope.launch {
            addedItems
                .map { it.at(0) }
                .collect(::onItemAdded)
        })

        eventJobs.add(viewModelScope.launch {
            updatedItems
                .map { it.at(getPosition(it.item)) }
                .filter { it.position != -1 }
                .collect(::onItemUpdated)
        })

        eventJobs.add(viewModelScope.launch {
            removedItems
                .map { it.at(getPosition(it.item)) }
                .filter { it.position != -1 }
                .collect(::onItemRemoved)
        })
    }

    suspend fun onItemAdded(event: PositionalEvent<Item>) {
        addItem(event)
        mAddedPositions.emit(event)
    }

    suspend fun onItemUpdated(event: PositionalEvent<Item>) {
        updateItem(event)
        mUpdatedPositions.emit(event)
    }

    suspend fun onItemRemoved(event: PositionalEvent<Item>) {
        removeItem(event)
        mRemovedPositions.emit(event)
    }
}
