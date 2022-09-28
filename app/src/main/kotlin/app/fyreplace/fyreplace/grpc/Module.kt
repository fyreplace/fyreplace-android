package app.fyreplace.fyreplace.grpc

import android.content.Context
import android.content.SharedPreferences
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object GrpcModule {
    @Provides
    fun provideChannel(
        @ApplicationContext context: Context,
        preferences: SharedPreferences
    ): Channel {
        val environment = preferences.getString(
            "app.environment",
            context.getString(R.string.settings_environment_default_value)
        )
        val host = when (environment) {
            context.getString(R.string.settings_environment_main_value) -> R.string.api_host_main
            context.getString(R.string.settings_environment_dev_value) -> R.string.api_host_dev
            context.getString(R.string.settings_environment_local_value) -> R.string.api_host_local
            else -> R.string.api_host_default
        }
        val channel = ManagedChannelBuilder.forAddress(
            context.resources.getString(host),
            context.resources.getInteger(R.integer.api_port)
        )

        if (environment == context.getString(R.string.settings_environment_local_value)) {
            channel.usePlaintext()
        }

        return channel
            .enableRetry()
            .intercept(listOf(AuthenticationInterceptor(preferences)))
            .build()
    }

    @Provides
    fun provideAccountServiceStub(channel: Channel) =
        AccountServiceGrpcKt.AccountServiceCoroutineStub(channel)

    @Provides
    fun provideUserServiceStub(channel: Channel) =
        UserServiceGrpcKt.UserServiceCoroutineStub(channel)

    @Provides
    fun providePostServiceStub(channel: Channel) =
        PostServiceGrpcKt.PostServiceCoroutineStub(channel)

    @Provides
    fun provideChapterServiceStub(channel: Channel) =
        ChapterServiceGrpcKt.ChapterServiceCoroutineStub(channel)

    @Provides
    fun provideCommentServiceStub(channel: Channel) =
        CommentServiceGrpcKt.CommentServiceCoroutineStub(channel)

    @Provides
    fun provideNotificationServiceStub(channel: Channel) =
        NotificationServiceGrpcKt.NotificationServiceCoroutineStub(channel)
}
