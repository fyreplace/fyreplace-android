package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.ComponentCallbacks2
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.legacy.extensions.authenticate
import app.fyreplace.fyreplace.legacy.extensions.dedupe
import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcStreamingCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel : ViewModel(), ComponentCallbacks2 {
    protected abstract val preferences: SharedPreferences

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() = Unit

    override fun onTrimMemory(level: Int) = Unit

    override fun onConfigurationChanged(newConfig: Configuration) = Unit

    fun <T> Flow<T>.asState(initialValue: T) =
        stateIn(viewModelScope, SharingStarted.Lazily, initialValue)

    suspend fun <S : Any, R : Any> GrpcCall<S, R>.executeFully(s: S) =
        authenticate(preferences).dedupe().execute(s)

    fun <S : Any, R : Any> GrpcStreamingCall<S, R>.executeFully() =
        authenticate(preferences).dedupe().executeIn(viewModelScope)
}
