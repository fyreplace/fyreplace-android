package app.fyreplace.fyreplace.api

import android.content.Context
import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.UsersEndpointApi
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.extensions.orDefault
import app.fyreplace.fyreplace.protos.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.openapitools.client.infrastructure.ApiClient
import java.util.UUID
import javax.inject.Inject

interface ApiResolver {
    suspend fun tokens(): TokensEndpointApi

    suspend fun users(): UsersEndpointApi
}

class RemoteApiResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resolver: StoreResolver,
    private val secretsHandler: SecretsHandler
) : ApiResolver {
    override suspend fun tokens() = get(TokensEndpointApi::class.java)

    override suspend fun users() = get(UsersEndpointApi::class.java)

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
        .combine(resolver.secretsStore.data) { url, secrets ->
            ApiClient(
                baseUrl = url,
                okHttpClientBuilder = OkHttpClient()
                    .newBuilder()
                    .addInterceptor(RequestIdInterceptor()),
                authName = "SecurityScheme",
                bearerToken = when {
                    secrets.token.isEmpty -> ""
                    else -> secretsHandler.decrypt(secrets.token)
                }
            )
        }
        .map { it.createService(clazz) }
        .first()
}

class RequestIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = with(chain.request()) {
        chain.proceed(
            newBuilder()
                .header(
                    HEADER_NAME,
                    header(HEADER_NAME) ?: UUID.randomUUID().toString()
                )
                .build()
        )
    }

    private companion object {
        const val HEADER_NAME = "X-Request-Id"
    }
}
