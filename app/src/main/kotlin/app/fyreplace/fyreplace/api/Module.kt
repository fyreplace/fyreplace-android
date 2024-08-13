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
import app.fyreplace.fyreplace.data.StoreResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object Module {
    @Provides
    fun provideChaptersEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<ChaptersEndpointApi> =
        RemoteEndpoint(context, resolver, ChaptersEndpointApi::class.java)

    @Provides
    fun provideCommentsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<CommentsEndpointApi> =
        RemoteEndpoint(context, resolver, CommentsEndpointApi::class.java)

    @Provides
    fun provideEmailsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<EmailsEndpointApi> =
        RemoteEndpoint(context, resolver, EmailsEndpointApi::class.java)

    @Provides
    fun providePostsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<PostsEndpointApi> =
        RemoteEndpoint(context, resolver, PostsEndpointApi::class.java)

    @Provides
    fun provideReportsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<ReportsEndpointApi> =
        RemoteEndpoint(context, resolver, ReportsEndpointApi::class.java)

    @Provides
    fun provideSubscriptionsEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<SubscriptionsEndpointApi> =
        RemoteEndpoint(context, resolver, SubscriptionsEndpointApi::class.java)

    @Provides
    fun provideTokensEndpointApi(
        @ApplicationContext context: Context, resolver: StoreResolver
    ): Endpoint<TokensEndpointApi> =
        RemoteEndpoint(context, resolver, TokensEndpointApi::class.java)

    @Provides
    fun provideUsersEndpointApi(
        @ApplicationContext context: Context,
        resolver: StoreResolver
    ): Endpoint<UsersEndpointApi> =
        RemoteEndpoint(context, resolver, UsersEndpointApi::class.java)
}
