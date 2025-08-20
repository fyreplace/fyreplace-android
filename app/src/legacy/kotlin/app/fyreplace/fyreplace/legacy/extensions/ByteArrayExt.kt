package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.fyreplace.legacy.ui.ImageSelector
import app.fyreplace.protos.ImageChunk
import app.fyreplace.protos.imageChunk
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

val ByteArray?.imageChunkFlow: Flow<ImageChunk>
    get() = when (this) {
        null -> emptyFlow()
        else -> flow {
            asIterable()
                .chunked(ImageSelector.IMAGE_CHUNK_SIZE)
                .map { imageChunk { data = ByteString.copyFrom(it.toByteArray()) } }
                .forEach { emit(it) }
        }
    }
