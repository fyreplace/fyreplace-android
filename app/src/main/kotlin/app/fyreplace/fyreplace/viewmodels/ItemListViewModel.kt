package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class ItemListViewModel<Item : Any, Items : Any> : BaseViewModel() {
    private var maybePages = MutableSharedFlow<Page?>(replay = 10)
    private var nextCursor = cursor { isNext = true }
    private var state = ItemsState.INCOMPLETE
    private val mItems = mutableListOf<Item>()
    protected val pages
        get() = flow { maybePages.takeWhile { it != null }.mapNotNull { it }.collect(::emit) }
    protected open val forward = false
    val items: List<Item> = mItems

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
            .onEach {
                nextCursor = getNextCursor(it)
                state = if (hasNextCursor(it)) ItemsState.INCOMPLETE else ItemsState.COMPLETE
            }
            .map { getItemList(it) }
            .onEach { mItems += it }
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
    }

    suspend fun fetchMore() {
        if (state == ItemsState.INCOMPLETE) {
            state = ItemsState.FETCHING
            maybePages.emit(page { cursor = nextCursor })
        }
    }

    fun add(position: Int, item: Item) {
        mItems.add(position, item)
    }

    fun remove(position: Int) {
        mItems.removeAt(position)
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
