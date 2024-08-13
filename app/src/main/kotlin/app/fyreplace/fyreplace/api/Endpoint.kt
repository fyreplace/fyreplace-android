package app.fyreplace.fyreplace.api

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.protos.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.openapitools.client.infrastructure.ApiClient

interface Endpoint<E> {
    suspend fun get(): E
}

class RemoteEndpoint<E>(
    @ApplicationContext private val context: Context,
    private val resolver: StoreResolver,
    private val clazz: Class<E>
) : Endpoint<E> {
    override suspend fun get() = resolver.connectionStore
        .data
        .map {
            when (it.environment) {
                Environment.MAIN -> R.string.api_url_main
                Environment.DEV -> R.string.api_url_dev
                Environment.LOCAL -> R.string.api_url_local
                else -> throw IllegalArgumentException("Unrecognized environment")
            }
        }
        .map(context::getString)
        .map(::ApiClient)
        .map { it.createService(clazz) }
        .first()
}
