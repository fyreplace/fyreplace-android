package app.fyreplace.fyreplace.api

import android.content.Context
import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.extensions.orDefault
import app.fyreplace.fyreplace.protos.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Inject

interface ApiResolver {
    suspend fun tokens(): TokensEndpointApi
}

class RemoteApiResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resolver: StoreResolver,
    private val secretsHandler: SecretsHandler
) : ApiResolver {
    override suspend fun tokens() = get(TokensEndpointApi::class.java)

    private suspend fun <T> get(clazz: Class<T>) = resolver.connectionStore
        .data
        .map {
            when (it.environment.orDefault) {
                Environment.MAIN -> R.string.api_url_main
                Environment.DEV -> R.string.api_url_dev
                Environment.LOCAL -> R.string.api_url_local
                else -> throw IllegalArgumentException("Unrecognized environment")
            }
        }
        .map(context::getString)
        .map(::ApiClient)
        .combine(resolver.secretsStore.data) { apiClient, secrets ->
            apiClient.apply {
                if (!secrets.token.isEmpty) {
                    setBearerToken(secretsHandler.decrypt(secrets.token))
                }
            }
        }
        .map { it.createService(clazz) }
        .first()
}
