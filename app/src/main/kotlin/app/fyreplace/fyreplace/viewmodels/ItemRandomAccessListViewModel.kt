package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.ItemEvent
import app.fyreplace.protos.Page
import app.fyreplace.protos.header
import app.fyreplace.protos.page
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemRandomAccessListViewModel<Item, Items>(
    em: EventsManager,
    private val contextId: ByteString
) :
    DynamicListViewModel<Item>(em) {
    override val removedItems = emptyFlow<ItemEvent<Item>>()
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var state = ItemListViewModel.ItemsState.COMPLETE
    private val mItems = mutableMapOf<Int, Item>()
    private val itemPositions = mutableMapOf<ByteString, Int>()
    private val positions = mutableListOf<Int>()
    private var mTotalSize = 0
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
    val items: Map<Int, Item> = mItems
    val totalSize get() = mTotalSize

    protected abstract fun listItems(): Flow<Items>

    protected abstract fun getItemList(items: Items): List<Item>

    protected abstract fun getTotalSize(items: Items): Int

    override fun getPosition(item: Item) = itemPositions[getItemId(item)] ?: -1

    override fun addItem(position: Int, item: Item) {
        mItems[totalSize] = item
        itemPositions[getItemId(item)] = totalSize
        mTotalSize++
    }

    override fun updateItem(position: Int, item: Item) {
        mItems[position] = item
        itemPositions[getItemId(item)] = position
    }

    override fun removeItem(position: Int, item: Item) = Unit

    suspend fun startListing(): Flow<Pair<Int, List<Item>>> {
        maybePages.emit(page {
            header = header {
                forward = true
                size = PAGE_SIZE
                contextId = this@ItemRandomAccessListViewModel.contextId
            }
        })
        state = ItemListViewModel.ItemsState.INCOMPLETE

        return listItems()
            .onEach {
                mTotalSize = getTotalSize(it)
                state = when {
                    positions.size > 1 -> ItemListViewModel.ItemsState.FETCHING
                    items.size < totalSize -> ItemListViewModel.ItemsState.INCOMPLETE
                    else -> ItemListViewModel.ItemsState.COMPLETE
                }
            }
            .map { getItemList(it) }
            .map {
                val position = positions.removeFirst()
                it.forEachIndexed { i, item ->
                    mItems[position + i] = item
                    itemPositions[getItemId(item)] = position + i
                }

                if (positions.isNotEmpty()) {
                    maybePages.emit(page { offset = positions.first() })
                }

                return@map position to it
            }
            .flowOn(Dispatchers.Main.immediate)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopListing() {
        state = ItemListViewModel.ItemsState.COMPLETE
        maybePages.tryEmit(null)
        maybePages.resetReplayCache()
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
            maybePages.emit(page { offset = startPosition })
        }
    }

    companion object {
        const val PAGE_SIZE = 12
    }
}
