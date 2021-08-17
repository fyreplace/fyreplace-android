package app.fyreplace.client.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.fyreplace.client.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface FailureHandler : BasePresenter, LifecycleOwner {
    fun onFailure(failure: Throwable) {
        getContext()?.run {
            Log.e(getString(R.string.app_name), failure.message.orEmpty())
            Toast.makeText(
                this,
                getString(R.string.error_title, failure.localizedMessage),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun launch(
        context: CoroutineContext = Dispatchers.Main.immediate,
        block: suspend CoroutineScope.() -> Unit
    ) = lifecycleScope.launch(context) {
        try {
            block()
        } catch (e: CancellationException) {
            // Cancellation is a normal occurrence
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun <T> Flow<T>.launchCollect(action: suspend (T) -> Unit) = launch { collect(action) }
}
