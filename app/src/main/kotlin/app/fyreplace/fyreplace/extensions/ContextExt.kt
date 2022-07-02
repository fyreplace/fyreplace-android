package app.fyreplace.fyreplace.extensions

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.fyreplace.fyreplace.R

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
