package app.fyreplace.fyreplace.legacy.extensions

import android.util.Base64
import okio.ByteString
import okio.ByteString.Companion.toByteString

val ByteString.base64ShortString: String
    get() = Base64.encodeToString(toByteArray(), Base64.NO_PADDING or Base64.URL_SAFE).trim()

fun byteString(base64ShortString: String) = try {
    Base64.decode(base64ShortString, Base64.NO_PADDING or Base64.URL_SAFE)
} catch (_: IllegalArgumentException) {
    byteArrayOf()
}.toByteString()
