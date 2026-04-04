package app.fyreplace.fyreplace.extensions

import okio.ByteString

val ByteString.isEmpty get() = size == 0
