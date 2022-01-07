package app.fyreplace.client.ui

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.fyreplace.client.R
import app.fyreplace.client.isNotNullOrBlank
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface FailureHandler : BasePresenter, LifecycleOwner {
    val preferences: SharedPreferences

    fun getFailureTexts(error: Status): Pair<Int, Int>? = null

    fun onFailure(failure: Throwable) {
        val error = Status.fromThrowable(failure)
        val (title, message) = getFailureTexts(error) ?: return rootView?.context?.run {
            Log.e(getString(R.string.app_name), failure.message.orEmpty())
            Toast.makeText(
                this,
                getString(R.string.error_title, failure.localizedMessage),
                Toast.LENGTH_LONG
            ).show()
        } ?: Unit
        showBasicAlert(title, message, error = true)
    }

    fun launch(
        scope: CoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.Main.immediate,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(context) {
        try {
            block()
        } catch (e: CancellationException) {
            // Cancellation is a normal occurrence
        } catch (e: StatusException) {
            onGrpcFailure(StatusRuntimeException(e.status))
        } catch (e: StatusRuntimeException) {
            onGrpcFailure(e)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun onGrpcFailure(e: StatusRuntimeException) {
        val isAuthenticated = preferences.getString("auth.token", null).isNotNullOrBlank()

        when {
            e.status.code == Status.Code.UNAVAILABLE -> showBasicAlert(
                R.string.error_unavailable_title,
                R.string.error_unavailable_message,
                error = true
            )
            e.status.code == Status.Code.UNAUTHENTICATED
                    && e.status.description !in setOf("timestamp_exceeded", "invalid_token")
                    && isAuthenticated -> preferences.edit { putString("auth.token", "") }
            else -> onFailure(e)
        }
    }

    fun <T> Flow<T>.launchCollect(
        scope: CoroutineScope = lifecycleScope,
        action: FlowCollector<T>
    ) = launch(scope) { collect(action) }
}
