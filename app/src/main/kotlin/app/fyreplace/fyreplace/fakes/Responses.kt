package app.fyreplace.fyreplace.fakes

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.Response.error

fun <T> ok(body: T): Response<T> = Response.success(body)

fun <T> created(body: T): Response<T> = Response.success(body)

fun <T> badRequest(): Response<T> = error(400, "Bad Request".toResponseBody())

fun <T> forbidden(): Response<T> = error(403, "Forbidden".toResponseBody())

fun <T> notFound(): Response<T> = error(404, "Not Found".toResponseBody())

fun <T> conflict(): Response<T> = error(409, "Conflict".toResponseBody())

fun <T> payloadTooLarge(): Response<T> = error(413, "Payload Too Large".toResponseBody())

fun <T> unsupportedMediaType(): Response<T> = error(415, "Unsupported Media Type".toResponseBody())
