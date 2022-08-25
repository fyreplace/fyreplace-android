package app.fyreplace.fyreplace.viewmodels

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel : ViewModel(), ComponentCallbacks2 {
    override fun onLowMemory() = Unit

    override fun onTrimMemory(level: Int) = Unit

    override fun onConfigurationChanged(newConfig: Configuration) = Unit

    fun <T> Flow<T>.asState(initialValue: T) =
        stateIn(viewModelScope, SharingStarted.Lazily, initialValue)
}
