package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.data.NewTokenCreation
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.fyreplace.fakes.badRequest
import app.fyreplace.fyreplace.fakes.created
import app.fyreplace.fyreplace.fakes.notFound
import app.fyreplace.fyreplace.fakes.ok

class FakeTokensEndpointApi : TokensEndpointApi {
    override suspend fun createNewToken(
        newTokenCreation: NewTokenCreation,
        customDeepLinks: Boolean?
    ) = when (newTokenCreation.identifier) {
        GOOD_IDENTIFIER -> ok(Unit)
        else -> notFound()
    }

    override suspend fun createToken(tokenCreation: TokenCreation) = when {
        tokenCreation.identifier != GOOD_IDENTIFIER -> notFound()
        tokenCreation.secret != GOOD_SECRET -> badRequest()
        else -> created(TOKEN)
    }

    override suspend fun getNewToken() = ok(TOKEN)

    companion object {
        const val BAD_IDENTIFIER = FakeUsersEndpointApi.BAD_EMAIL
        const val GOOD_IDENTIFIER = FakeUsersEndpointApi.GOOD_EMAIL
        const val BAD_SECRET = "nopenope"
        const val GOOD_SECRET = "abcd1234"
        const val TOKEN = "token"
    }
}
