package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemListViewModel<Item, Items> : BaseViewModel() {
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var nextCursor = cursor { isNext = true }
    private var state = ItemsState.INCOMPLETE
    private val mItems = mutableListOf<Item>()
    private val mIsEmpty = MutableStateFlow(true)
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
    protected open val forward = false
    val items: List<Item> = mItems
    val isEmpty = mIsEmpty.asStateFlow()

    protected abstract fun listItems(): Flow<Items>

    protected abstract fun hasNextCursor(items: Items): Boolean

    protected abstract fun getNextCursor(items: Items): Cursor

    protected abstract fun getItemList(items: Items): List<Item>

    suspend fun startListing(): Flow<List<Item>> {
        maybePages.emit(page {
            header = header {
                forward = this@ItemListViewModel.forward
                size = PAGE_SIZE
            }
        })

        return listItems()
            .filter { state == ItemsState.FETCHING }
            .onEach {
                nextCursor = getNextCursor(it)
                state = if (hasNextCursor(it)) ItemsState.INCOMPLETE else ItemsState.COMPLETE
            }
            .map { getItemList(it) }
            .onEach {
                mItems += it
                mIsEmpty.value = items.isEmpty()
            }
            .flowOn(Dispatchers.Main.immediate)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopListing() {
        maybePages.tryEmit(null)
        maybePages.resetReplayCache()
    }

    fun reset() {
        nextCursor = cursor { isNext = true }
        state = ItemsState.INCOMPLETE
        mItems.clear()
        mIsEmpty.value = true
    }

    suspend fun fetchMore() {
        if (state == ItemsState.INCOMPLETE) {
            state = ItemsState.FETCHING
            maybePages.emit(page { cursor = nextCursor })
        }
    }

    fun add(position: Int, item: Item) {
        mItems.add(position, item)
        mIsEmpty.value = false
    }

    fun update(position: Int, item: Item) {
        mItems[position] = item
    }

    fun remove(position: Int) {
        mItems.removeAt(position)
        mIsEmpty.value = items.isEmpty()
    }

    companion object {
        const val PAGE_SIZE = 12
    }

    enum class ItemsState {
        INCOMPLETE,
        COMPLETE,
        FETCHING
    }
}
