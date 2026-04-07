package app.fyreplace.fyreplace.legacy.viewmodels

import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.ItemEvent
import app.fyreplace.fyreplace.legacy.events.PositionalEvent
import app.fyreplace.protos.Header
import app.fyreplace.protos.Page
import com.squareup.wire.GrpcStreamingCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.ByteString

abstract class ItemRandomAccessListViewModel<Item, Items : Any>(
    em: EventsManager,
    private val contextId: ByteString
) : DynamicListViewModel<Item>(em) {
    override val removedItems = emptyFlow<ItemEvent<Item>>()
    private lateinit var pagesChannel: SendChannel<Page>
    private var state = ItemListViewModel.ItemsState.PAUSED
    private val mItems = mutableMapOf<Int, Item>()
    private val itemPositions = mutableMapOf<ByteString, Int>()
    private val positions = mutableListOf<Int>()
    private var mTotalSize = 0
    val items: Map<Int, Item> = mItems
    val totalSize get() = mTotalSize

    protected abstract fun listItems(): GrpcStreamingCall<Page, Items>

    protected abstract fun getItemList(items: Items): List<Item>

    protected abstract fun getTotalSize(items: Items): Int

    override fun getPosition(item: Item) = itemPositions[getItemId(item)] ?: -1

    override fun addItem(event: PositionalEvent<Item>) {
        mItems[totalSize] = event.event.item
        itemPositions[getItemId(event.event.item)] = totalSize
        mTotalSize++
    }

    override fun updateItem(event: PositionalEvent<Item>) {
        mItems[event.position] = event.event.item
        itemPositions[getItemId(event.event.item)] = event.position
    }

    override fun removeItem(event: PositionalEvent<Item>) = Unit

    suspend fun startListing(): Flow<Pair<Int, List<Item>>> {
        if (state == ItemListViewModel.ItemsState.PAUSED) {
            state = ItemListViewModel.ItemsState.INCOMPLETE
        }

        val (sender, receiver) = listItems().executeFully()
        pagesChannel = sender
        pagesChannel.send(
            Page(
                header_ = Header(
                    forward = true,
                    size = PAGE_SIZE,
                    context_id = contextId
                )
            )
        )

        return flow {
            for (newItems in receiver) {
                emitItems(newItems)
            }
        }
            .flowOn(Dispatchers.Main.immediate)
    }

    fun stopListing() {
        if (state != ItemListViewModel.ItemsState.COMPLETE) {
            state = ItemListViewModel.ItemsState.PAUSED
        }

        pagesChannel.close()
    }

    open fun reset() {
        state = ItemListViewModel.ItemsState.INCOMPLETE
        mItems.clear()
        itemPositions.clear()
        positions.clear()
        mTotalSize = 0
    }

    suspend fun fetchAround(position: Int) {
        if (state == ItemListViewModel.ItemsState.COMPLETE) {
            return
        }

        val startPosition = position - (position % PAGE_SIZE)

        if (startPosition !in positions) {
            positions.add(startPosition)
        }

        if (state == ItemListViewModel.ItemsState.INCOMPLETE) {
            state = ItemListViewModel.ItemsState.FETCHING
            pagesChannel.send(Page(offset = startPosition))
        }
    }

    private suspend fun FlowCollector<Pair<Int, List<Item>>>.emitItems(newItems: Items) {
        mTotalSize = getTotalSize(newItems)
        state = when {
            positions.size > 1 -> ItemListViewModel.ItemsState.FETCHING
            items.size < totalSize -> ItemListViewModel.ItemsState.INCOMPLETE
            else -> ItemListViewModel.ItemsState.COMPLETE
        }

        val position = positions.removeAt(0)
        val newItemsList = getItemList(newItems)
        newItemsList.forEachIndexed { i, item ->
            mItems[position + i] = item
            itemPositions[getItemId(item)] = position + i
        }

        if (positions.isNotEmpty()) {
            pagesChannel.send(Page(offset = positions.first()))
        }

        emit(position to newItemsList)
    }

    companion object {
        const val PAGE_SIZE = 12
    }
}
