package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.datastore.core.DataStore
import app.fyreplace.fyreplace.protos.Account
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.Secrets
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface StoreResolver {
    val connectionStore: DataStore<Connection>
    val accountStore: DataStore<Account>
    val secretsStore: DataStore<Secrets>
}

class ContextStoreResolver @Inject constructor(
    @ApplicationContext context: Context
) : StoreResolver {
    override val connectionStore = context.connectionStore
    override val accountStore = context.accountStore
    override val secretsStore = context.secretsStore
}
