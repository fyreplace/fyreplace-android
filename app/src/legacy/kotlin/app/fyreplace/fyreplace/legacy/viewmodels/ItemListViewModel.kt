package app.fyreplace.fyreplace.legacy.viewmodels

import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.PositionalEvent
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Header
import app.fyreplace.protos.Page
import com.squareup.wire.GrpcStreamingCall
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

abstract class ItemListViewModel<Item, Items : Any>(em: EventsManager) :
    DynamicListViewModel<Item>(em) {
    private lateinit var pagesChannel: SendChannel<Page>
    private var nextCursor: Cursor? = Cursor(is_next = true)
    private var state = ItemsState.PAUSED
    private val mItems = mutableListOf<Item>()
    private val mIsEmpty = MutableStateFlow(true)
    private var mManuallyAddedCount = 0
    protected open val forward = false
    val items: List<Item> = mItems
    val isEmpty = mIsEmpty.asStateFlow()
    val manuallyAddedCount get() = mManuallyAddedCount
    abstract val emptyText: StateFlow<Int>

    override fun onTrimMemory(level: Int) = reset()

    override fun getPosition(item: Item): Int {
        val itemId = getItemId(item)
        return mItems.indexOfFirst { getItemId(it) == itemId }
    }

    override fun addItem(event: PositionalEvent<Item>) {
        mItems.add(event.position, event.event.item)
        mIsEmpty.value = false
        mManuallyAddedCount++
    }

    override fun updateItem(event: PositionalEvent<Item>) {
        mItems[event.position] = event.event.item
    }

    override fun removeItem(event: PositionalEvent<Item>) {
        mItems.removeAt(event.position)
        mIsEmpty.value = items.isEmpty()
        mManuallyAddedCount--
    }

    protected abstract fun listItems(): GrpcStreamingCall<Page, Items>

    protected abstract fun getNextCursor(items: Items): Cursor?

    protected abstract fun getItemList(items: Items): List<Item>

    suspend fun startListing(): Flow<List<Item>> {
        if (state == ItemsState.PAUSED) {
            state = ItemsState.INCOMPLETE
        }

        val (sender, receiver) = listItems().executeFully()
        pagesChannel = sender
        pagesChannel.send(
            Page(
                header_ = Header(
                    forward = forward,
                    size = PAGE_SIZE
                )
            )
        )

        return flow {
            for (newItems in receiver) {
                emitItems(newItems)
            }
        }
    }

    fun stopListing() {
        if (state != ItemsState.COMPLETE) {
            state = ItemsState.PAUSED
        }

        pagesChannel.close()
    }

    fun reset() {
        nextCursor = Cursor(is_next = true)
        state = ItemsState.INCOMPLETE
        mItems.clear()
        mIsEmpty.value = true
        mManuallyAddedCount = 0
    }

    suspend fun fetchMore() {
        if (state == ItemsState.INCOMPLETE) {
            state = ItemsState.FETCHING
            pagesChannel.send(Page(cursor = nextCursor))
        }
    }

    private suspend fun FlowCollector<List<Item>>.emitItems(newItems: Items) {
        if (state != ItemsState.FETCHING) {
            return
        }

        nextCursor = getNextCursor(newItems)
        state = if (nextCursor != null) ItemsState.INCOMPLETE else ItemsState.COMPLETE
        val newItemsList = getItemList(newItems)
        mItems += newItemsList
        mIsEmpty.value = items.isEmpty()
        emit(newItemsList)
    }

    companion object {
        const val PAGE_SIZE = 12
    }

    enum class ItemsState {
        INCOMPLETE,
        COMPLETE,
        FETCHING,
        PAUSED
    }
}
