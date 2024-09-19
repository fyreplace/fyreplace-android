package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.data.NewTokenCreation
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.fyreplace.fakes.badRequest
import app.fyreplace.fyreplace.fakes.created
import app.fyreplace.fyreplace.fakes.forbidden
import app.fyreplace.fyreplace.fakes.notFound
import app.fyreplace.fyreplace.fakes.ok

class FakeTokensEndpointApi : TokensEndpointApi {
    override suspend fun createNewToken(
        newTokenCreation: NewTokenCreation,
        customDeepLinks: Boolean?
    ) = when (newTokenCreation.identifier) {
        in GOOD_IDENTIFIERS -> ok(Unit)
        PASSWORD_IDENTIFIER -> forbidden()
        else -> notFound()
    }

    override suspend fun createToken(tokenCreation: TokenCreation) = when {
        tokenCreation.identifier !in GOOD_IDENTIFIERS -> notFound()
        tokenCreation.secret != GOOD_SECRET -> badRequest()
        else -> created(TOKEN)
    }

    override suspend fun getNewToken() = ok(TOKEN)

    companion object {
        val GOOD_IDENTIFIERS =
            setOf(FakeUsersEndpointApi.GOOD_EMAIL, FakeUsersEndpointApi.GOOD_USERNAME)
        const val PASSWORD_IDENTIFIER = FakeUsersEndpointApi.PASSWORD_USERNAME
        const val BAD_SECRET = "nopenope"
        const val GOOD_SECRET = "abcd1234"
        const val TOKEN = "token"
    }
}
