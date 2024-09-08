package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.fakes.api.FakeTokensEndpointApi
import app.fyreplace.fyreplace.fakes.api.FakeUsersEndpointApi

class FakeApiResolver : ApiResolver {
    override suspend fun tokens() = FakeTokensEndpointApi()

    override suspend fun users() = FakeUsersEndpointApi()
}
