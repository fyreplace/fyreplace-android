package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import app.fyreplace.fyreplace.BuildConfig
import app.fyreplace.fyreplace.protos.Connection
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ConnectionSerializer : Serializer<Connection> {
    override val defaultValue = Connection(environment = BuildConfig.ENVIRONMENT_DEFAULT)

    override suspend fun readFrom(input: InputStream): Connection {
        try {
            return Connection.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Connection, output: OutputStream) =
        Connection.ADAPTER.encode(output, t)
}

val Context.connectionStore by dataStore(
    fileName = "connection.pb",
    serializer = ConnectionSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { ConnectionSerializer.defaultValue }
)
