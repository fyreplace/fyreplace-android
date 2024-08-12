package app.fyreplace.fyreplace.api

import android.content.Context
import app.fyreplace.api.ChaptersEndpointApi
import app.fyreplace.api.CommentsEndpointApi
import app.fyreplace.api.EmailsEndpointApi
import app.fyreplace.api.PostsEndpointApi
import app.fyreplace.api.ReportsEndpointApi
import app.fyreplace.api.SubscriptionsEndpointApi
import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.UsersEndpointApi
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.protos.Environment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.openapitools.client.infrastructure.ApiClient

@Module
@InstallIn(ViewModelComponent::class)
object Module {
    @Provides
    fun provideChaptersEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<ChaptersEndpointApi> =
        EndpointImpl(context, resolver, ChaptersEndpointApi::class.java)

    @Provides
    fun provideCommentsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<CommentsEndpointApi> =
        EndpointImpl(context, resolver, CommentsEndpointApi::class.java)

    @Provides
    fun provideEmailsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<EmailsEndpointApi> =
        EndpointImpl(context, resolver, EmailsEndpointApi::class.java)

    @Provides
    fun providePostsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<PostsEndpointApi> =
        EndpointImpl(context, resolver, PostsEndpointApi::class.java)

    @Provides
    fun provideReportsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<ReportsEndpointApi> =
        EndpointImpl(context, resolver, ReportsEndpointApi::class.java)

    @Provides
    fun provideSubscriptionsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<SubscriptionsEndpointApi> =
        EndpointImpl(context, resolver, SubscriptionsEndpointApi::class.java)

    @Provides
    fun provideTokensEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<TokensEndpointApi> =
        EndpointImpl(context, resolver, TokensEndpointApi::class.java)

    @Provides
    fun provideUsersEndpointApi(
        @ApplicationContext context: Context,
        resolver: StoreResolver
    ): Endpoint<UsersEndpointApi> =
        EndpointImpl(context, resolver, UsersEndpointApi::class.java)
}

interface Endpoint<E> {
    suspend fun get(): E
}

class EndpointImpl<E>(
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
