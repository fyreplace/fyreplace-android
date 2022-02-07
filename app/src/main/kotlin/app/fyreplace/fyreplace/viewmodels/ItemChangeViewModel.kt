package app.fyreplace.fyreplace.viewmodels

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

abstract class ItemChangeViewModel<Item> : BaseViewModel() {
    private val mAddedItems = MutableSharedFlow<Pair<Int, Item>>()
    private val mUpdatedItems = MutableSharedFlow<Pair<Int, Item>>()
    private val mRemovedPositions = MutableSharedFlow<Int>()
    val addedItems = mAddedItems.filter { it.first != -1 }
    val updatedItems = mUpdatedItems.filter { it.first != -1 }
    val removedPositions = mRemovedPositions.filter { it != -1 }

    suspend fun add(position: Int, item: Item) = mAddedItems.emit(position to item)

    suspend fun update(position: Int, item: Item) = mUpdatedItems.emit(position to item)

    suspend fun delete(position: Int) = mRemovedPositions.emit(position)
}
