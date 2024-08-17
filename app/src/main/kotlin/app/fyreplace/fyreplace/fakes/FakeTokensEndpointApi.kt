package app.fyreplace.fyreplace.fakes

import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.data.NewTokenCreation
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.fyreplace.api.Endpoint
import retrofit2.Response
import retrofit2.Response.success

class FakeTokensEndpointApi : TokensEndpointApi, Endpoint<TokensEndpointApi> {
    override suspend fun createNewToken(newTokenCreation: NewTokenCreation): Response<Unit> =
        when (newTokenCreation.identifier) {
            GOOD_IDENTIFIER -> success(Unit)
            else -> notFound()
        }

    override suspend fun createToken(tokenCreation: TokenCreation): Response<String> =
        when {
            tokenCreation.identifier == GOOD_IDENTIFIER && tokenCreation.secret == GOOD_SECRET ->
                success(GOOD_TOKEN)

            else -> notFound()
        }

    override suspend fun getNewToken(): Response<String> = success(GOOD_TOKEN)

    override suspend fun get() = this

    companion object {
        const val BAD_IDENTIFIER = "bad-identifier"
        const val GOOD_IDENTIFIER = "good-identifier"
        const val BAD_SECRET = "000000"
        const val GOOD_SECRET = "123456"
        const val BAD_TOKEN = "bad-token"
        const val GOOD_TOKEN = "good-token"
    }
}
