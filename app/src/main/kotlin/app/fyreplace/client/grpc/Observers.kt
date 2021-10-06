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

class ResponsesObserver<T> : StreamObserver<T> {
    private val responses = mutableListOf<T>()
    private var error: Throwable? = null
    private var stop = false
    private var continuation: Continuation<T>? = null
        set(value) {
            field = value
            field?.run {
                error?.let { resumeWithException(it) } ?: if (responses.size > 0) {
                    resume(responses.removeFirst())
                }
            }
        }

    override fun onNext(value: T) {
        error = null
        continuation?.resume(value) ?: responses.add(value)
    }

    override fun onError(t: Throwable) {
        error = t
        continuation?.resumeWithException(t)
    }

    override fun onCompleted() {
        stop = true
    }

    suspend fun awaitNext() = when {
        stop -> throw IllegalStateException()
        else -> withContext(Dispatchers.IO) { suspendCoroutine<T> { continuation = it } }
    }
}

suspend fun <Response> awaitImageUpload(
    call: KFunction1<StreamObserver<Response>, StreamObserver<ImageChunk>>,
    image: ByteArray?
) {
    val responseObserver = ResponsesObserver<Response>()
    val requestObserver = call(responseObserver)
    image?.asIterable()
        ?.chunked(ImageSelector.IMAGE_CHUNK_SIZE)
        ?.map { ImageChunk.newBuilder().setData(ByteString.copyFrom(it.toByteArray())).build() }
        ?.forEach {
            coroutineContext.ensureActive()
            requestObserver.onNext(it)
        }
    requestObserver.onCompleted()
    responseObserver.awaitNext()
}
