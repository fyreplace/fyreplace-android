package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.EmailsEndpointApi
import app.fyreplace.api.data.Email
import app.fyreplace.api.data.EmailCreation
import app.fyreplace.api.data.EmailVerification
import app.fyreplace.fyreplace.fakes.make
import app.fyreplace.fyreplace.fakes.ok
import retrofit2.Response
import java.util.UUID

class FakeEmailsEndpointApi : EmailsEndpointApi {
    override suspend fun countEmails() = ok(listEmails(null).body()!!.size.toLong())

    override suspend fun createEmail(
        emailCreation: EmailCreation,
        customDeepLinks: Boolean?
    ): Response<Email> =
        throw NotImplementedError()

    override suspend fun deleteEmail(id: UUID): Response<Unit> =
        throw NotImplementedError()

    override suspend fun listEmails(page: Int?) = when (page) {
        null, 0 -> ok(listOf(Email.make(main = true), Email.make(), Email.make()))
        else -> ok(emptyList())
    }

    override suspend fun setMainEmail(id: UUID): Response<Unit> =
        throw NotImplementedError()

    override suspend fun verifyEmail(emailVerification: EmailVerification): Response<Unit> =
        throw NotImplementedError()
}
