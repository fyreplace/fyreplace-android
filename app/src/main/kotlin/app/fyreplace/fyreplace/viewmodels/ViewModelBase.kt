package app.fyreplace.fyreplace.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class ViewModelBase : ViewModel() {
    protected fun <T> Flow<T>.asState(initialValue: T) =
        mutableStateOf(initialValue).apply { viewModelScope.launch { collect { value = it } } }
}
