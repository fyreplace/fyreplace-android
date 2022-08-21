package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.PositionalEvent
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
    override val removedItems = emptyFlow<PositionalEvent>()
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var state = ItemListViewModel.ItemsState.INCOMPLETE
    private val mItems = mutableMapOf<Int, Item>()
    private val indexes = mutableListOf<Int>()
    private var mTotalSize = 0
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
    val items: Map<Int, Item> = mItems
    val totalSize get() = mTotalSize

    protected abstract fun listItems(): Flow<Items>

    protected abstract fun getItemList(items: Items): List<Item>

    protected abstract fun getTotalSize(items: Items): Int

    override fun addItem(position: Int, item: Item) {
        mItems[totalSize] = item
        mTotalSize++
    }

    override fun updateItem(position: Int, item: Item) {
        mItems[position] = item
    }

    override fun removeItem(position: Int) = Unit

    suspend fun startListing(): Flow<Pair<Int, List<Item>>> {
        maybePages.emit(page {
            header = header {
                forward = true
                size = PAGE_SIZE
                contextId = this@ItemRandomAccessListViewModel.contextId
            }
        })

        return listItems()
            .onEach {
                mTotalSize = getTotalSize(it)
                state = when {
                    indexes.size > 1 -> ItemListViewModel.ItemsState.FETCHING
                    items.size < totalSize -> ItemListViewModel.ItemsState.INCOMPLETE
                    else -> ItemListViewModel.ItemsState.COMPLETE
                }
            }
            .map { getItemList(it) }
            .map {
                val index = indexes.removeFirst()
                it.forEachIndexed { i, item -> mItems[index + i] = item }

                if (indexes.isNotEmpty()) {
                    maybePages.emit(page { offset = indexes.first() })
                }

                return@map index to it
            }
            .flowOn(Dispatchers.Main.immediate)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopListing() {
        maybePages.tryEmit(null)
        maybePages.resetReplayCache()
    }

    open fun reset() {
        state = ItemListViewModel.ItemsState.INCOMPLETE
        mItems.clear()
        indexes.clear()
        mTotalSize = 0
    }

    suspend fun fetchAround(index: Int) {
        if (state == ItemListViewModel.ItemsState.COMPLETE) {
            return
        }

        val startIndex = index - (index % PAGE_SIZE)
        indexes.add(startIndex)

        if (state == ItemListViewModel.ItemsState.INCOMPLETE) {
            state = ItemListViewModel.ItemsState.FETCHING
            maybePages.emit(page { offset = startIndex })
        }
    }

    companion object {
        const val PAGE_SIZE = 12
    }
}
