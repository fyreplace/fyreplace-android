package app.fyreplace.fyreplace.test.fakes

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.Response.error

fun <T> notFound(): Response<T> = error(404, "Not found".toResponseBody())
