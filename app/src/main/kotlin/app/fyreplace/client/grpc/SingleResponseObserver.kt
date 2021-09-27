package app.fyreplace.client.grpc

import app.fyreplace.client.ui.ImageSelector
import app.fyreplace.protos.ImageChunk
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.*
import kotlin.reflect.KFunction1
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

    suspend fun await() = withContext(Dispatchers.IO) { suspendCoroutine<T> { continuation = it } }
}

suspend fun <Request, Response> awaitSingleResponse(
    call: KFunction2<Request, StreamObserver<Response>, Unit>,
    request: Request
): Response {
    val observer = SingleResponseObserver<Response>()
    call(request, observer)
    return observer.await()
}

suspend fun <Response> awaitImageUpload(
    call: KFunction1<StreamObserver<Response>, StreamObserver<ImageChunk>>,
    image: ByteArray?
) {
    val responseObserver = SingleResponseObserver<Response>()
    val requestObserver = call(responseObserver)
    image?.asIterable()
        ?.chunked(ImageSelector.IMAGE_CHUNK_SIZE)
        ?.map { ImageChunk.newBuilder().setData(ByteString.copyFrom(it.toByteArray())).build() }
        ?.forEach {
            coroutineContext.ensureActive()
            requestObserver.onNext(it)
        }
    requestObserver.onCompleted()
    responseObserver.await()
}
