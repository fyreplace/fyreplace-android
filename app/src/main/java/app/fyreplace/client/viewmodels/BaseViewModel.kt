package app.fyreplace.client.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel : ViewModel() {
    fun <T> Flow<T>.asState(initialValue: T) =
        stateIn(viewModelScope, SharingStarted.Lazily, initialValue)
}
