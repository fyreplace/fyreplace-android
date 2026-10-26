package app.fyreplace.fyreplace.extensions

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.Response.error

fun <T> ok(body: T): Response<T> = Response.success(body)

fun <T> created(body: T): Response<T> = Response.success(
    body,
    okhttp3.Response.Builder()
        .code(201)
        .message("Created")
        .protocol(Protocol.HTTP_1_1)
        .request(Request.Builder().url("http://localhost/").build())
        .build()
)

fun noContent(): Response<Unit> = Response.success(Unit)

fun <T> badRequest(): Response<T> = error(400, "Bad Request".toResponseBody())

fun <T> forbidden(): Response<T> = error(403, "Forbidden".toResponseBody())

fun <T> notFound(): Response<T> = error(404, "Not Found".toResponseBody())

fun <T> conflict(): Response<T> = error(409, "Conflict".toResponseBody())

fun <T> payloadTooLarge(): Response<T> = error(413, "Payload Too Large".toResponseBody())

fun <T> unsupportedMediaType(): Response<T> = error(415, "Unsupported Media Type".toResponseBody())
