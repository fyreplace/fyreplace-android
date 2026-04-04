package app.fyreplace.fyreplace.fakes

import androidx.datastore.core.DataStore
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.protos.Account
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.CurrentUser
import app.fyreplace.fyreplace.protos.Environment
import app.fyreplace.fyreplace.protos.Secrets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeStoreResolver(
    connection: Connection = Connection(environment = Environment.LOCAL),
    account: Account = Account(),
    currentUser: CurrentUser = CurrentUser(),
    secrets: Secrets = Secrets()
) : StoreResolver {
    override val connectionStore = FakeSomethingStore(connection)

    override val accountStore = FakeSomethingStore(account)

    override val currentUserStore = FakeSomethingStore(currentUser)

    override val secretsStore = FakeSomethingStore(secrets)
}

class FakeSomethingStore<T>(initialValue: T) : DataStore<T> {
    private val mData = MutableStateFlow(initialValue)

    override val data = mData.asStateFlow()

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        mData.emit(transform(data.value))
        return data.value
    }
}
