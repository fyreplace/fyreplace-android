package app.fyreplace.fyreplace.viewmodels

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class ItemChangeViewModel<Item> : BaseViewModel() {
    private val mAddedItems = MutableSharedFlow<Pair<Int, Item>>()
    private val mRemovedPositions = MutableSharedFlow<Int>()
    val addedItems: Flow<Pair<Int, Item>> = mAddedItems
    val removedPositions: Flow<Int> = mRemovedPositions

    suspend fun add(position: Int, item: Item) = mAddedItems.emit(position to item)

    suspend fun delete(position: Int) = mRemovedPositions.emit(position)
}
