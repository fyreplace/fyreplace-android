package app.fyreplace.fyreplace.extensions

import android.content.res.Resources
import android.net.Uri
import android.util.Base64
import app.fyreplace.fyreplace.R
import com.google.protobuf.ByteString

fun makeShareUri(resources: Resources, type: String, id: ByteString): Uri {
    val host = resources.getString(R.string.link_host)
    val encodedId = Base64.encodeToString(id.toByteArray(), Base64.NO_PADDING)
    return Uri.parse("https://$host/$type/$encodedId")
}
