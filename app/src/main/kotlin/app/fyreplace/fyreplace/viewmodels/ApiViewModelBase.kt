package app.fyreplace.fyreplace.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.Endpoint
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.events.FailureEvent
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class ApiViewModelBase(private val eventBus: EventBus) : ViewModelBase() {
    abstract fun handle(failure: Failure): Event?

    fun <T> call(api: Endpoint<T>, block: suspend T.() -> Unit) {
        viewModelScope.launch {
            try {
                block(api.get())
            } catch (e: UnknownHostException) {
                postConnectionFailure()
            } catch (e: SocketTimeoutException) {
                postConnectionFailure()
            } catch (e: Exception) {
                eventBus.publish(FailureEvent())
                Sentry.captureException(e)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun <T> Response<T>.check(): T? {
        if (!isSuccessful) {
            val failure = Failure(
                code(),
                errorBody()?.let {
                    if (it.contentLength() > 0) {
                        Json.decodeFromStream(it.byteStream())
                    } else {
                        null
                    }
                }
            )

            val failureEvent = handle(failure)

            if (failureEvent != null) {
                eventBus.publish(failureEvent)
            }
        }

        return body()
    }

    private suspend fun postConnectionFailure() = eventBus.publish(
        FailureEvent(
            R.string.main_error_connection_title,
            R.string.main_error_connection_message
        )
    )
}

@Immutable
data class Failure(val code: Int, val explanation: ExplainedFailure?)

@Serializable
@Immutable
data class ExplainedFailure(val title: String, val reason: String)
