package app.fyreplace.client.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.fyreplace.client.grpc.ResponsesObserver
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Header
import app.fyreplace.protos.Page
import io.grpc.stub.StreamObserver

abstract class ItemListPagingSource<Item : Any, Items : Any> : PagingSource<Cursor, Item>() {
    private val responsesObserver = ResponsesObserver<Items>()
    private val requestsObserver by lazy { startListing(responsesObserver) }

    abstract fun startListing(observer: ResponsesObserver<Items>): StreamObserver<Page>

    abstract fun makeResult(items: Items): LoadResult.Page<Cursor, Item>

    override fun getRefreshKey(state: PagingState<Cursor, Item>) = state.anchorPosition?.let {
        when {
            it < state.config.pageSize -> null
            else -> state.closestPageToPosition(it - state.config.pageSize)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, Item> {
        if (params.key == null) {
            val header = Header.newBuilder().setForward(false).setSize(params.loadSize)
            val page = Page.newBuilder().setHeader(header).build()
            requestsObserver.onNext(page)
        }

        val cursor = params.key ?: Cursor.newBuilder().setIsNext(true).build()
        val page = Page.newBuilder().setCursor(cursor).build()
        requestsObserver.onNext(page)
        return makeResult(responsesObserver.awaitNext())
    }

    fun complete() = requestsObserver.onCompleted()
}
