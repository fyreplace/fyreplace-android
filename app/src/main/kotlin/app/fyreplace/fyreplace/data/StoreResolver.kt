package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import app.fyreplace.fyreplace.protos.Connection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

interface StoreResolver {
    fun connectionStoreIn(scope: CoroutineScope): DataStore<Connection>
}

class StoreResolverImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StoreResolver {
    override fun connectionStoreIn(scope: CoroutineScope) =
        StoreWrapper(context, ConnectionSerializer, scope).store

    class StoreWrapper<T>(context: Context, serializer: Serializer<T>, scope: CoroutineScope) {
        private val Context.store by dataStore(
            fileName = "connection.pb",
            serializer = serializer,
            corruptionHandler = ReplaceFileCorruptionHandler { serializer.defaultValue },
            scope = scope
        )

        val store = context.store
    }
}
