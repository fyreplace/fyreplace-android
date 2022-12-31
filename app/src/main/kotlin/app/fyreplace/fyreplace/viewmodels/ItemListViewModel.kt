package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.PositionalEvent
import app.fyreplace.protos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemListViewModel<Item, Items>(em: EventsManager) : DynamicListViewModel<Item>(em) {
    private val maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var nextCursor = cursor { isNext = true }
    private var state = ItemsState.PAUSED
    private val mItems = mutableListOf<Item>()
    private val mIsEmpty = MutableStateFlow(true)
    private var mManuallyAddedCount = 0
    protected val pages get() = maybePages.takeWhile { it != null }.filterNotNull()
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

        if (state == ItemsState.PAUSED) {
            state = ItemsState.INCOMPLETE
        }

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
        if (state != ItemsState.COMPLETE) {
            state = ItemsState.PAUSED
        }

        maybePages.tryEmit(null)
        maybePages.resetReplayCache()
    }

    fun reset() {
        nextCursor = cursor { isNext = true }
        state = ItemsState.INCOMPLETE
        mItems.clear()
        mIsEmpty.value = true
        mManuallyAddedCount = 0
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
        FETCHING,
        PAUSED
    }
}
