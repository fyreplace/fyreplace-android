package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.api.ApiResolver

class FakeApiResolver : ApiResolver {
    override suspend fun tokens() = FakeTokensEndpointApi()
}
