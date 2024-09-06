package app.fyreplace.fyreplace.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import app.fyreplace.api.data.ExplainedFailure
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import com.squareup.moshi.JsonClass
import io.sentry.Sentry
import kotlinx.coroutines.launch
import org.openapitools.client.infrastructure.getErrorResponse
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class ApiViewModelBase(
    protected val eventBus: EventBus,
    protected val storeResolver: StoreResolver
) : ViewModelBase() {
    fun <T> call(api: suspend () -> T, block: suspend T.() -> Unit) {
        viewModelScope.launch {
            try {
                block(api())
            } catch (e: UnknownHostException) {
                postConnectionFailure()
            } catch (e: SocketTimeoutException) {
                postConnectionFailure()
            } catch (e: Exception) {
                eventBus.publish(Event.Failure())
                Sentry.captureException(e)
            }
        }
    }

    suspend fun <T> Response<T>.failWith(failureHandler: (Failure) -> Event.Failure?): T? {
        if (isSuccessful) {
            return body()
        }

        if (code() == 401) {
            storeResolver.secretsStore.update { clearToken() }
            eventBus.publish(Event.Failure(R.string.error_401_title, R.string.error_401_message))
            return null
        }

        val failureEvent = failureHandler(
            Failure(
                code(),
                try {
                    getErrorResponse()
                } catch (_: Exception) {
                    null
                }
            )
        )

        if (failureEvent != null) {
            eventBus.publish(failureEvent)
        }

        return null
    }

    private suspend fun postConnectionFailure() = eventBus.publish(
        Event.Failure(
            R.string.main_error_connection_title,
            R.string.main_error_connection_message
        )
    )
}

@Immutable
@JsonClass(generateAdapter = true)
data class Failure(val code: Int, val explanation: ExplainedFailure?)
