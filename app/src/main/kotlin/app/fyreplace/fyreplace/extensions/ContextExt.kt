package app.fyreplace.fyreplace.extensions

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import app.fyreplace.fyreplace.R
import com.google.android.material.color.DynamicColors
import com.google.protobuf.ByteString

val Context.mainPreferences: SharedPreferences
    get() = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

fun Context.makeShareIntent(type: String, id: ByteString, position: Int? = null): Intent {
    val host = getString(R.string.link_host)
    var uriString = "https://$host/$type/${id.base64ShortString}"
    position?.let { uriString += "/$position" }
    val uri = Uri.parse(uriString)
    return Intent(Intent.ACTION_SEND).apply {
        this.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        putExtra(Intent.EXTRA_TEXT, uri.toString())
    }
}

fun Context.getDynamicColor(@AttrRes attr: Int, @ColorInt default: Int) = DynamicColors
    .wrapContextIfAvailable(this)
    .obtainStyledAttributes(intArrayOf(attr))
    .use { it.getColor(0, default) }
