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
    val totalSize: Flow<Int> = mTotalSize

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
                state = if (items.size < mTotalSize.value) ItemListViewModel.ItemsState.INCOMPLETE
                else ItemListViewModel.ItemsState.COMPLETE
            }
            .map { getItemList(it) }
            .map {
                val index = indexes.removeFirst()
                it.forEachIndexed { i, item -> mItems[index + i] = item }
                return@map index to it
            }
            .flowOn(Dispatchers.Main.immediate)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopListing() {
        maybePages.tryEmit(null)
        maybePages.resetReplayCache()
    }

    suspend fun fetchAround(index: Int) {
        if (state != ItemListViewModel.ItemsState.INCOMPLETE) {
            return
        }

        val startIndex = index - (index % 12)
        state = ItemListViewModel.ItemsState.FETCHING
        indexes.add(startIndex)
        maybePages.emit(page { offset = startIndex })
    }

    companion object {
        const val PAGE_SIZE = 12
    }
}
