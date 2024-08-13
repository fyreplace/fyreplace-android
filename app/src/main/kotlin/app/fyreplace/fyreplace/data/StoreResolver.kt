package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.DataStore
import app.fyreplace.fyreplace.protos.Connection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface StoreResolver {
    val connectionStore: DataStore<Connection>
}

class ContextStoreResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : StoreResolver {
    override val connectionStore = context.connectionStore
}
