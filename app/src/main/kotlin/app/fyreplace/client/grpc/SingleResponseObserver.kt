package app.fyreplace.client.grpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction2

class SingleResponseObserver<T> : StreamObserver<T> {
    private var nextValue: T? = null
    private var error: Throwable? = null
    private var continuation: Continuation<T>? = null
        set(value) {
            field = value
            nextValue?.let { field?.resume(it) }
            error?.let { field?.resumeWithException(it) }
        }

    override fun onNext(value: T) {
        nextValue = value
        error = null
        continuation?.resume(value)
    }

    override fun onError(t: Throwable) {
        nextValue = null
        error = t
        continuation?.resumeWithException(t)
    }

    override fun onCompleted() = Unit

    suspend fun await() = suspendCoroutine<T> { continuation = it }
}

suspend fun <Request, Response> awaitSingleResponse(
    call: KFunction2<Request, StreamObserver<Response>, Unit>,
    request: Request
): Response = withContext(Dispatchers.IO) {
    val observer = SingleResponseObserver<Response>()
    call(request, observer)
    return@withContext observer.await()
}
