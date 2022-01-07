package app.fyreplace.client.viewmodels

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class ItemDeletionViewModel : BaseViewModel() {
    private val mDeletedPositions = MutableSharedFlow<Int>()
    val deletedPositions: Flow<Int> = mDeletedPositions

    suspend fun delete(position: Int) = mDeletedPositions.emit(position)
}
