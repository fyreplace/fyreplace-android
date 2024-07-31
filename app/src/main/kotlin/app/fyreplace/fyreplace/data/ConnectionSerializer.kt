package app.fyreplace.fyreplace.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import app.fyreplace.fyreplace.BuildConfig
import app.fyreplace.fyreplace.protos.Connection
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ConnectionSerializer : Serializer<Connection> {
    override val defaultValue: Connection
        get() = Connection.getDefaultInstance()
            .toBuilder()
            .setEnvironment(BuildConfig.ENVIRONMENT_DEFAULT)
            .build()

    override suspend fun readFrom(input: InputStream): Connection {
        try {
            return Connection.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Connection, output: OutputStream) = t.writeTo(output)
}
