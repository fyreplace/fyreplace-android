package app.fyreplace.fyreplace.data

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.moveTo
import org.koin.dsl.module

val dataModule = module {
    single {
        get<Context>().run {
            val plainTextPrefs = getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
            )

            return@run if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    }
}
