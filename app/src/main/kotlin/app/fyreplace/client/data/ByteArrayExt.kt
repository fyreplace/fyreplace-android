package app.fyreplace.client.data

import app.fyreplace.client.ui.ImageSelector
import app.fyreplace.protos.ImageChunk
import app.fyreplace.protos.imageChunk
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.*

suspend fun ByteArray?.imageChunkFlow() = flow {
    this@imageChunkFlow?.asIterable()
        ?.chunked(ImageSelector.IMAGE_CHUNK_SIZE)
        ?.map { imageChunk { data = ByteString.copyFrom(it.toByteArray()) } }
        ?.forEach { this.emit(it) } ?: emptyFlow<ImageChunk>()
}
