package app.fyreplace.client.viewmodels

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.fyreplace.client.data.ItemListPagingSource

abstract class ItemListViewModel<Item : Any, Items : Any> : BaseViewModel() {
    val items = Pager(pagingConfig) {
        makeSource().also {
            currentSource?.complete()
            currentSource = it
        }
    }.flow.cachedIn(viewModelScope)
    private var currentSource: ItemListPagingSource<Item, Items>? = null

    override fun onCleared() {
        super.onCleared()
        currentSource?.complete()
    }

    abstract fun makeSource(): ItemListPagingSource<Item, Items>

    private companion object {
        val pagingConfig = PagingConfig(
            pageSize = 12,
            initialLoadSize = 12,
            enablePlaceholders = false
        )
    }
}
