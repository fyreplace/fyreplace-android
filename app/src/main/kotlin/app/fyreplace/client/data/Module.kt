package app.fyreplace.client.data

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import app.fyreplace.client.R
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
                    get(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ).also { plainTextPrefs.moveTo(it) }
            } else {
                plainTextPrefs
            }
        }
    }
}
