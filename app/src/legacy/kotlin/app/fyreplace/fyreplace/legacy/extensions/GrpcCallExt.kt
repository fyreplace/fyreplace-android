package app.fyreplace.fyreplace.legacy.extensions

import android.content.SharedPreferences
import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcStreamingCall
import java.util.UUID

fun <S : Any, R : Any> GrpcCall<S, R>.authenticate(preferences: SharedPreferences) =
    apply { requestMetadata = requestMetadata.authenticated(preferences) }

fun <S : Any, R : Any> GrpcCall<S, R>.dedupe() =
    apply { requestMetadata = requestMetadata.deduplicated() }

fun <S : Any, R : Any> GrpcStreamingCall<S, R>.authenticate(preferences: SharedPreferences) =
    apply { requestMetadata = requestMetadata.authenticated(preferences) }

fun <S : Any, R : Any> GrpcStreamingCall<S, R>.dedupe() = apply {
    requestMetadata = requestMetadata.deduplicated()
}

fun Map<String, String>.authenticated(preferences: SharedPreferences): Map<String, String> {
    val token = preferences.getString("auth.token", null) ?: return this
    return this + ("authorization" to "Bearer $token")
}

fun Map<String, String>.deduplicated() =
    this + ("x-request-id" to UUID.randomUUID().toString())
