package app.fyreplace.fyreplace.fakes

import androidx.datastore.core.DataStore
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeStoreResolver : StoreResolver {
    override val connectionStore = FakeConnectionStore()
}

class FakeConnectionStore : DataStore<Connection> {
    private val mData = MutableStateFlow(
        Connection.newBuilder()
            .setEnvironment(Environment.LOCAL)
            .build()
    )

    override val data = mData.asStateFlow()

    override suspend fun updateData(transform: suspend (t: Connection) -> Connection): Connection {
        mData.emit(transform(data.value))
        return data.value
    }
}
