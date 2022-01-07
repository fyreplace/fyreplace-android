package app.fyreplace.fyreplace.grpc

import android.content.SharedPreferences
import io.grpc.*
import io.grpc.ClientInterceptors.CheckedForwardingClientCall

class AuthenticationInterceptor(private val preferences: SharedPreferences) : ClientInterceptor {
    override fun <Request, Response> interceptCall(
        method: MethodDescriptor<Request, Response>,
        callOptions: CallOptions,
        next: Channel
    ) = AuthenticatedCall(next.newCall(method, callOptions), preferences)
}

class AuthenticatedCall<Request, Response>(
    delegate: ClientCall<Request, Response>,
    private val preferences: SharedPreferences
) :
    CheckedForwardingClientCall<Request, Response>(delegate) {
    override fun checkedStart(responseListener: Listener<Response>, headers: Metadata) {
        preferences.getString("auth.token", null)
            ?.takeIf { it.isNotBlank() }
            ?.let {
                val key = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
                headers.put(key, "Bearer $it")
            }

        delegate().start(responseListener, headers)
    }
}
