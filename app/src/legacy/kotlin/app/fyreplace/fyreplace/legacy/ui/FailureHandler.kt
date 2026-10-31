package app.fyreplace.fyreplace.legacy.ui

import android.content.ComponentCallbacks
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.extensions.PREF_AUTH_TOKEN
import app.fyreplace.fyreplace.legacy.extensions.mainPreferences
import com.squareup.wire.GrpcException
import com.squareup.wire.GrpcStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

interface FailureHandler : BasePresenter, LifecycleOwner, ComponentCallbacks {
    val preferences get() = getContext()?.mainPreferences

    fun getFailureTexts(error: GrpcException): Pair<Int, Int>? = null

    fun onFailure(error: GrpcException) {
        val errorMessage =
            error.grpcMessage ?: error.localizedMessage ?: error.toString()
        val (title, message) = getFailureTexts(error) ?: return getContext()?.run {
            Log.e(getString(R.string.app_name), errorMessage)
            Toast.makeText(
                this,
                getString(R.string.error_title, errorMessage),
                Toast.LENGTH_LONG
            ).show()
        } ?: Unit
        showBasicAlert(title, message, error = true)
    }

    fun launch(
        scope: CoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.Main.immediate,
        retry: (() -> Unit)? = null,
        autoDisconnect: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(context) {
        try {
            block()
        } catch (_: CancellationException) {
            // Cancellation is a normal occurrence
        } catch (e: GrpcException) {
            onGrpcFailure(e, retry, autoDisconnect)
        } catch (e: Exception) {
            onFailure(
                GrpcException(
                    grpcStatus = GrpcStatus.UNKNOWN,
                    grpcMessage = e.localizedMessage
                )
            )
        }
    }

    private suspend fun onGrpcFailure(
        error: GrpcException,
        retry: (() -> Unit)?,
        autoDisconnect: Boolean
    ) = when (error.grpcStatus) {
        GrpcStatus.UNAVAILABLE ->
            if (retry != null) {
                delay(500.milliseconds)
                retry()
            } else {
                showBasicAlert(
                    R.string.error_unavailable_title,
                    R.string.error_unavailable_message,
                    error = true
                )
            }

        GrpcStatus.UNAUTHENTICATED if autoDisconnect -> {
            if (preferences?.getString(PREF_AUTH_TOKEN, null)?.isNotEmpty() == true) {
                showBasicAlert(
                    R.string.error_autodisconnect_title,
                    R.string.error_autodisconnect_message,
                    error = true
                )
            }

            preferences?.edit { putString(PREF_AUTH_TOKEN, "") }
        }

        else -> onFailure(error)
    }

    fun <T> Flow<T>.launchCollect(
        scope: CoroutineScope = lifecycleScope,
        retry: (() -> Unit)? = null,
        action: FlowCollector<T>
    ) = launch(scope, retry = retry) { collect(action) }
}
