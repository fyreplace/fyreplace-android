package app.fyreplace.fyreplace.extensions

import android.util.Base64
import com.google.protobuf.ByteString

val ByteString.base64ShortString: String
    get() = Base64.encodeToString(toByteArray(), Base64.NO_PADDING)

fun byteString(base64ShortString: String): ByteString =
    ByteString.copyFrom(Base64.decode(base64ShortString, Base64.NO_PADDING))
