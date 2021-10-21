package app.fyreplace.client.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.fyreplace.protos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ItemListPagingSource<Item : Any, Items : Any> : PagingSource<Cursor, Item>() {
    abstract val itemsFlow: Flow<Items>
    protected val cursorFlow = flow {
        maybeCursorFlow.takeWhile { it != null }.mapNotNull { it }.collect(::emit)
    }
    private val maybeCursorFlow = MutableSharedFlow<Page?>(replay = Int.MAX_VALUE)
    private val itemsList = mutableListOf<Items>()
    private var continuation: Continuation<Items>? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var flowCollected = false

    abstract fun makeResult(items: Items): LoadResult.Page<Cursor, Item>

    override fun getRefreshKey(state: PagingState<Cursor, Item>) = state.anchorPosition?.let {
        when {
            it < state.config.pageSize -> null
            else -> state.closestPageToPosition(it - state.config.pageSize)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, Item> {
        if (!flowCollected) {
            scope.launch { itemsFlow.collect(::addItems) }
            flowCollected = true
        }

        if (params.key == null) {
            val pageHeader = header {
                forward = false
                size = params.loadSize
            }
            maybeCursorFlow.emit(page { header = pageHeader })
        }

        maybeCursorFlow.emit(page { cursor = params.key ?: cursor { isNext = true } })
        return makeResult(awaitNextItems())
    }

    fun complete() {
        maybeCursorFlow.tryEmit(null)
    }

    private fun addItems(items: Items) {
        continuation?.resume(items) ?: itemsList.add(items)
    }

    private suspend fun awaitNextItems() = suspendCoroutine<Items> {
        if (itemsList.isNotEmpty()) {
            it.resume(itemsList.removeFirst())
        } else {
            continuation = it
        }
    }
}
