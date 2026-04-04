package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import app.fyreplace.fyreplace.protos.CurrentUser
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object CurrentUserSerializer : Serializer<CurrentUser> {
    override val defaultValue = CurrentUser()

    override suspend fun readFrom(input: InputStream): CurrentUser {
        try {
            return CurrentUser.ADAPTER.decode(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: CurrentUser, output: OutputStream) =
        CurrentUser.ADAPTER.encode(output, t)
}

val Context.currentUserStore by dataStore(
    fileName = "current_user.pb",
    serializer = CurrentUserSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { CurrentUserSerializer.defaultValue }
)
