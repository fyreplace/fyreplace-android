package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import app.fyreplace.fyreplace.protos.Secrets
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object SecretSerializer : Serializer<Secrets> {
    override val defaultValue = Secrets()

    override suspend fun readFrom(input: InputStream): Secrets {
        try {
            return Secrets.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Secrets, output: OutputStream) =
        Secrets.ADAPTER.encode(output, t)
}

val Context.secretsStore by dataStore(
    fileName = "secrets.pb",
    serializer = SecretSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SecretSerializer.defaultValue }
)
