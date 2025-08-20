package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.EmailsEndpointApi
import app.fyreplace.api.data.Email
import app.fyreplace.api.data.EmailCreation
import app.fyreplace.api.data.EmailVerification
import app.fyreplace.fyreplace.extensions.badRequest
import app.fyreplace.fyreplace.extensions.conflict
import app.fyreplace.fyreplace.extensions.make
import app.fyreplace.fyreplace.extensions.notFound
import app.fyreplace.fyreplace.extensions.ok
import retrofit2.Response
import java.util.UUID

class FakeEmailsEndpointApi : EmailsEndpointApi {
    override suspend fun countEmails() = ok(listEmails(null).body()!!.size.toLong())

    override suspend fun createEmail(
        emailCreation: EmailCreation,
        customDeepLinks: Boolean?
    ): Response<Email> =
        when (emailCreation.email) {
            FakeUsersEndpointApi.BAD_EMAIL -> badRequest()
            FakeUsersEndpointApi.USED_EMAIL -> conflict()
            else -> ok(
                Email(
                    id = UUID.randomUUID(),
                    email = emailCreation.email,
                    main = false,
                    verified = false
                )
            )
        }

    override suspend fun deleteEmail(id: UUID): Response<Unit> =
        throw NotImplementedError()

    override suspend fun listEmails(page: Int?) = when (page) {
        null, 0 -> ok(listOf(Email.make(main = true), Email.make(), Email.make(verified = false)))
        else -> ok(emptyList())
    }

    override suspend fun setMainEmail(id: UUID): Response<Unit> =
        throw NotImplementedError()

    override suspend fun verifyEmail(emailVerification: EmailVerification) =
        when (emailVerification.code) {
            FakeTokensEndpointApi.GOOD_SECRET -> ok(Unit)
            else -> notFound()
        }
}
