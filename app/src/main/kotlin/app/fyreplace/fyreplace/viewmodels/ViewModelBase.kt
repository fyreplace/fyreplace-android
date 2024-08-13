package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

abstract class ViewModelBase : ViewModel() {
    fun <T> Flow<T>.asState(initialValue: T) =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initialValue)
}
