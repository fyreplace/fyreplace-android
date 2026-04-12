package app.fyreplace.fyreplace.legacy.grpc

import android.content.Context
import android.content.SharedPreferences
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.AccountServiceClient
import app.fyreplace.protos.ChapterServiceClient
import app.fyreplace.protos.CommentServiceClient
import app.fyreplace.protos.NotificationServiceClient
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.UserServiceClient
import com.squareup.wire.GrpcClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object GrpcModule {
    private fun SharedPreferences.getEnvironment(context: Context) = getString(
        "app.environment",
        context.getString(R.string.settings_environment_default_value)
    )

    @Provides
    fun provideHttpClient(
        @ApplicationContext context: Context,
        preferences: SharedPreferences
    ): OkHttpClient {
        val localEnvironment = context.getString(R.string.settings_environment_local_value)
        val protocols = if (preferences.getEnvironment(context) == localEnvironment) {
            listOf(Protocol.H2_PRIOR_KNOWLEDGE)
        } else {
            listOf(Protocol.HTTP_1_1, Protocol.HTTP_2)
        }

        return OkHttpClient.Builder()
            .protocols(protocols)
            .build()
    }

    @Provides
    fun provideGrpcClient(
        @ApplicationContext context: Context,
        preferences: SharedPreferences,
        httpClient: OkHttpClient
    ): GrpcClient {
        val environment = preferences.getEnvironment(context)
        val localEnvironment = context.getString(R.string.settings_environment_local_value)
        val isLocal = environment == localEnvironment
        val (hostRes, portRes) = when (environment) {
            context.getString(R.string.settings_environment_main_value) -> R.string.api_host_main to R.integer.api_port_main
            context.getString(R.string.settings_environment_dev_value) -> R.string.api_host_dev to R.integer.api_port_dev
            localEnvironment -> R.string.api_host_local to R.integer.api_port_local
            else -> throw RuntimeException("Invalid environment")
        }

        val serverUrl = HttpUrl.Builder()
            .scheme(if (isLocal) "http" else "https")
            .host(context.resources.getString(hostRes))
            .port(context.resources.getInteger(portRes))
            .build()

        return GrpcClient.Builder()
            .client(httpClient)
            .baseUrl(serverUrl)
            .build()
    }

    @Provides
    fun provideAccountServiceStub(client: GrpcClient) =
        client.create(AccountServiceClient::class)

    @Provides
    fun provideUserServiceStub(client: GrpcClient) =
        client.create(UserServiceClient::class)

    @Provides
    fun providePostServiceStub(client: GrpcClient) =
        client.create(PostServiceClient::class)

    @Provides
    fun provideChapterServiceStub(client: GrpcClient) =
        client.create(ChapterServiceClient::class)

    @Provides
    fun provideCommentServiceStub(client: GrpcClient) =
        client.create(CommentServiceClient::class)

    @Provides
    fun provideNotificationServiceStub(client: GrpcClient) =
        client.create(NotificationServiceClient::class)
}
