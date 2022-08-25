package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.protos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemListViewModel<Item, Items>(em: EventsManager) : DynamicListViewModel<Item>(em) {
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var nextCursor = cursor { isNext = true }
    private var state = ItemsState.INCOMPLETE
    private val mItems = mutableListOf<Item>()
    private val mIsEmpty = MutableStateFlow(true)
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
    protected open val forward = false
    val items: List<Item> = mItems
    val isEmpty = mIsEmpty.asStateFlow()
    abstract val emptyText: StateFlow<Int>

    override fun onTrimMemory(level: Int) = reset()

    override fun addItem(position: Int, item: Item) {
        mItems.add(position, item)
        mIsEmpty.value = false
    }

    override fun updateItem(position: Int, item: Item) {
        mItems[position] = item
    }

    override fun removeItem(position: Int) {
        mItems.removeAt(position)
        mIsEmpty.value = items.isEmpty()
    }

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

    companion object {
        const val PAGE_SIZE = 12
    }

    enum class ItemsState {
        INCOMPLETE,
        COMPLETE,
        FETCHING
    }
}
