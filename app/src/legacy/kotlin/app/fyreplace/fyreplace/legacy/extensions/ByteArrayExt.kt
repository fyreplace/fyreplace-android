package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.fyreplace.legacy.ui.ImageSelector
import app.fyreplace.protos.ImageChunk
import okio.ByteString.Companion.toByteString

val ByteArray?.imageChunks: Iterable<ImageChunk>
    get() = when (this) {
        null -> emptyList()
        else -> asIterable()
            .chunked(ImageSelector.IMAGE_CHUNK_SIZE)
            .map { ImageChunk(data_ = it.toByteArray().toByteString()) }
    }

