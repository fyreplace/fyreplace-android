package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Page
import app.fyreplace.protos.header
import app.fyreplace.protos.page
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemRandomAccessListViewModel<Item : Any, Items : Any>(
    private val contextId: ByteString
) : BaseViewModel() {
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var state = ItemListViewModel.ItemsState.INCOMPLETE
    private val mItems = mutableMapOf<Int, Item>()
    private val indexes = mutableListOf<Int>()
    private val mTotalSize = MutableStateFlow(0)
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
    val items: Map<Int, Item> = mItems
    val totalSize = mTotalSize.asStateFlow()

    protected abstract fun listItems(): Flow<Items>

    protected abstract fun getItemList(items: Items): List<Item>

    protected abstract fun getTotalSize(items: Items): Int

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
                mTotalSize.value = getTotalSize(it)
                state = when {
                    indexes.size > 1 -> ItemListViewModel.ItemsState.FETCHING
                    items.size < mTotalSize.value -> ItemListViewModel.ItemsState.INCOMPLETE
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
        mTotalSize.value = 0
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

    fun insert(item: Item) {
        mItems[totalSize.value] = item
        mTotalSize.value++
    }

    fun update(position: Int, item: Item) {
        mItems[position] = item
    }

    companion object {
        const val PAGE_SIZE = 12
    }
}
