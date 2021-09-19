package app.fyreplace.client.grpc

import android.content.res.Resources
import app.fyreplace.client.R
import app.fyreplace.protos.*
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannelBuilder
import org.koin.dsl.module

val grpcModule = module {
    single<List<ClientInterceptor>> { listOf(AuthenticationInterceptor(get())) }

    single<Channel> {
        val resources = get<Resources>()
        val channel = ManagedChannelBuilder.forAddress(
            resources.getString(R.string.api_host),
            resources.getInteger(R.integer.api_port)
        )

        if (resources.getBoolean(R.bool.clear_text_communication)) {
            channel.usePlaintext()
        }

        return@single channel
            .enableRetry()
            .intercept(get<List<ClientInterceptor>>())
            .build()
    }

    factory { AccountServiceGrpc.newStub(get()) }
    factory { UserServiceGrpc.newStub(get()) }
    factory { PostServiceGrpc.newStub(get()) }
    factory { ChapterServiceGrpc.newStub(get()) }
    factory { CommentServiceGrpc.newStub(get()) }
    factory { NotificationServiceGrpc.newStub(get()) }
}
