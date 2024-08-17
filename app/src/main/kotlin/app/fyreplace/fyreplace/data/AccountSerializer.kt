package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import app.fyreplace.fyreplace.protos.Account
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object AccountSerializer : Serializer<Account> {
    override val defaultValue: Account
        get() = Account.getDefaultInstance()
            .toBuilder()
            .build()

    override suspend fun readFrom(input: InputStream): Account {
        try {
            return Account.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Account, output: OutputStream) = t.writeTo(output)
}

val Context.accountStore by dataStore(
    fileName = "account.pb",
    serializer = AccountSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AccountSerializer.defaultValue }
)
