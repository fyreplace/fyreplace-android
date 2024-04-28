package app.fyreplace.fyreplace.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors.CheckedForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import java.util.UUID

class RequestIdentificationInterceptor : ClientInterceptor {
    override fun <Request, Response> interceptCall(
        method: MethodDescriptor<Request, Response>,
        callOptions: CallOptions,
        next: Channel
    ) = IdentifiedCall<Request, Response>(next.newCall(method, callOptions))
}

class IdentifiedCall<Request, Response>(delegate: ClientCall<Request, Response>) :
    CheckedForwardingClientCall<Request, Response>(delegate) {
    override fun checkedStart(responseListener: Listener<Response>, headers: Metadata) {
        val key = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)
        headers.put(key, UUID.randomUUID().toString())
        delegate().start(responseListener, headers)
    }
}
