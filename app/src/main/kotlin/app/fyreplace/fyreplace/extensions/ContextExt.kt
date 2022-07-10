package app.fyreplace.fyreplace.extensions

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.fyreplace.fyreplace.R
import com.google.protobuf.ByteString

val Context.mainPreferences: SharedPreferences
    get() {
        val plainTextPrefs = getSharedPreferences(
            getString(R.string.app_name),
            Context.MODE_PRIVATE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EncryptedSharedPreferences.create(
                packageName,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { plainTextPrefs.moveTo(it) }
        } else {
            plainTextPrefs
        }
    }

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
