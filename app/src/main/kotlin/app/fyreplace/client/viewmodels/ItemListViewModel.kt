package app.fyreplace.client.viewmodels

import app.fyreplace.protos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class ItemListViewModel<Item : Any, Items : Any> : BaseViewModel() {
    protected val pages = flow {
        maybePages.takeWhile { it != null }.mapNotNull { it }.collect(::emit)
    }
    private var maybePages = MutableSharedFlow<Page?>(replay = Int.MAX_VALUE)
    private var nextCursor = cursor { isNext = true }
    private var fetching = false
    private var endReached = false

    protected abstract fun listItems(): Flow<Items>

    protected abstract fun hasNextCursor(items: Items): Boolean

    protected abstract fun getNextCursor(items: Items): Cursor

    protected abstract fun getItemList(items: Items): List<Item>

    suspend fun startListing(): Flow<List<Item>> {
        maybePages.emit(page { header = header { forward = false; size = pageSize } })
        fetchMore()
        return listItems()
            .onEach {
                nextCursor = getNextCursor(it)
                fetching = false

                if (!hasNextCursor(it)) {
                    endReached = true
                }
            }
            .map { getItemList(it) }
            .flowOn(Dispatchers.Main.immediate)
    }

    fun stopListing() {
        maybePages.tryEmit(null)
    }

    fun reset() {
        nextCursor = cursor { isNext = true }
        fetching = false
        endReached = false
    }

    suspend fun fetchMore() {
        if (fetching || endReached) {
            return
        }

        fetching = true
        maybePages.emit(page { cursor = nextCursor })
    }

    companion object {
        const val pageSize = 12
    }
}
