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
        val protocol = if (preferences.getEnvironment(context) == localEnvironment) {
            Protocol.H2_PRIOR_KNOWLEDGE
        } else {
            Protocol.HTTP_2
        }

        return OkHttpClient.Builder()
            .protocols(listOf(protocol))
            .build()
    }

    @Provides
    fun provideGrpcClient(
        @ApplicationContext context: Context,
        preferences: SharedPreferences,
        httpClient: OkHttpClient
    ): GrpcClient {
        val host = when (preferences.getEnvironment(context)) {
            context.getString(R.string.settings_environment_main_value) -> R.string.api_host_main
            context.getString(R.string.settings_environment_dev_value) -> R.string.api_host_dev
            context.getString(R.string.settings_environment_local_value) -> R.string.api_host_local
            else -> R.string.api_host_default
        }

        val serverUrl = HttpUrl.Builder()
            .scheme("http")
            .host(context.resources.getString(host))
            .port(context.resources.getInteger(R.integer.api_port))
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
