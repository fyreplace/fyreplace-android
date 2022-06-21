package app.fyreplace.fyreplace.extensions

import android.content.res.Resources
import android.net.Uri
import app.fyreplace.fyreplace.R
import com.google.protobuf.ByteString

fun makeShareUri(resources: Resources, type: String, id: ByteString): Uri {
    val host = resources.getString(R.string.link_host)
    return Uri.parse("https://$host/$type/${id.base64ShortString}")
}
